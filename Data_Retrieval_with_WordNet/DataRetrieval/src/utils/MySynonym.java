package utils;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.synonym.SolrSynonymParser;
import org.apache.lucene.analysis.synonym.SynonymGraphFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.analysis.synonym.WordnetSynonymParser;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoaderAware;
import org.apache.lucene.analysis.util.TokenFilterFactory;
import org.apache.lucene.analysis.util.TokenizerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class MySynonym extends TokenFilterFactory implements ResourceLoaderAware {
    private final boolean ignoreCase;
    private final String tokenizerFactory;
    private final String synonyms;
    private final String format;
    private final boolean expand;
    private final String analyzerName;
    private final Map<String, String> tokArgs = new HashMap();
    private SynonymMap map;

    public MySynonym(Map<String, String> args) {
        super(args);
        this.ignoreCase = this.getBoolean(args, "ignoreCase", false);
        this.synonyms = this.require(args, "synonyms");
        this.format = this.get(args, "format");
        this.expand = this.getBoolean(args, "expand", false);
        this.analyzerName = this.get(args, "analyzer");
        this.tokenizerFactory = this.get(args, "tokenizerFactory");
        if (this.analyzerName != null && this.tokenizerFactory != null) {
            throw new IllegalArgumentException("Analyzer and TokenizerFactory can't be specified both: " + this.analyzerName + " and " + this.tokenizerFactory);
        } else {
            if (this.tokenizerFactory != null) {
                this.tokArgs.put("luceneMatchVersion", this.getLuceneMatchVersion().toString());
                Iterator itr = args.keySet().iterator();

                while(itr.hasNext()) {
                    String key = (String)itr.next();
                    this.tokArgs.put(key.replaceAll("^tokenizerFactory\\.", ""), args.get(key));
                    itr.remove();
                }
            }

            if (!args.isEmpty()) {
                throw new IllegalArgumentException("Unknown parameters: " + args);
            }
        }
    }

    public TokenStream create(TokenStream input) {
        return (TokenStream)(this.map.fst == null ? input : new SynonymGraphFilter(input, this.map, this.ignoreCase));
    }

    public void inform(ResourceLoader loader) throws IOException {
        final TokenizerFactory factory = this.tokenizerFactory == null ? null : this.loadTokenizerFactory(loader, this.tokenizerFactory);
        Analyzer analyzer;
        if (this.analyzerName != null) {
            analyzer = this.loadAnalyzer(loader, this.analyzerName);
        } else {
            analyzer = new Analyzer() {
                protected TokenStreamComponents createComponents(String fieldName) {
                    Tokenizer tokenizer = factory == null ? new WhitespaceTokenizer() : factory.create();
                    TokenStream stream = MySynonym.this.ignoreCase ? new LowerCaseFilter((TokenStream)tokenizer) : tokenizer;
                    return new TokenStreamComponents((Tokenizer)tokenizer, (TokenStream)stream);
                }
            };
        }

        try {
            Analyzer a = analyzer;
            Throwable var5 = null;

            try {
                String formatClass = this.format;
                if (this.format != null && !this.format.equals("solr")) {
                    if (this.format.equals("wordnet")) {
                        formatClass = WordnetSynonymParser.class.getName();
                    }
                } else {
                    formatClass = SolrSynonymParser.class.getName();
                }

                this.map = this.loadSynonyms(loader, formatClass, true, a);
            } catch (Throwable var15) {
                var5 = var15;
                throw var15;
            } finally {
                if (analyzer != null) {
                    if (var5 != null) {
                        try {
                            a.close();
                        } catch (Throwable var14) {
                            var5.addSuppressed(var14);
                        }
                    } else {
                        analyzer.close();
                    }
                }

            }

        } catch (ParseException var17) {
            throw new IOException("Error parsing synonyms file:", var17);
        }
    }

    protected SynonymMap loadSynonyms(ResourceLoader loader, String cname, boolean dedup, Analyzer analyzer) throws IOException, ParseException {
        CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
        Class clazz = loader.findClass(cname, SynonymMap.Parser.class);

        SynonymMap.Parser parser;
        try {
            parser = (SynonymMap.Parser)clazz.getConstructor(Boolean.TYPE, Boolean.TYPE, Analyzer.class).newInstance(dedup, this.expand, analyzer);
        } catch (Exception var11) {
            throw new RuntimeException(var11);
        }

        List<String> files = this.splitFileNames(this.synonyms);
        Iterator var9 = files.iterator();

        while(var9.hasNext()) {
            String file = (String)var9.next();
            decoder.reset();
            parser.parse(new InputStreamReader(loader.openResource(file), decoder));
        }

        return parser.build();
    }

    private TokenizerFactory loadTokenizerFactory(ResourceLoader loader, String cname) throws IOException {
        Class clazz = loader.findClass(cname, TokenizerFactory.class);

        try {
            TokenizerFactory tokFactory = (TokenizerFactory)clazz.getConstructor(Map.class).newInstance(this.tokArgs);
            if (tokFactory instanceof ResourceLoaderAware) {
                ((ResourceLoaderAware)tokFactory).inform(loader);
            }

            return tokFactory;
        } catch (Exception var5) {
            throw new RuntimeException(var5);
        }
    }

    private Analyzer loadAnalyzer(ResourceLoader loader, String cname) throws IOException {
        Class clazz = loader.findClass(cname, Analyzer.class);

        try {
            Analyzer analyzer = (Analyzer)clazz.getConstructor().newInstance();
            if (analyzer instanceof ResourceLoaderAware) {
                ((ResourceLoaderAware)analyzer).inform(loader);
            }

            return analyzer;
        } catch (Exception var5) {
            throw new RuntimeException(var5);
        }
    }
}