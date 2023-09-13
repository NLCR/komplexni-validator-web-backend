package nkp.pspValidator.web.backend.cli;

import nkp.pspValidator.web.backend.utils.auth.KeyBuilder;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class App {
    public static void main(String[] args) {
        Options options = new Options();
        options.addOption(OptionBuilder
                .withLongOpt(Params.HELP)
                .withDescription(replaceUmlaut("Zobrazit nápovědu."))
                .create());
        options.addOption(OptionBuilder
                .withLongOpt(Params.VERSION)
                .withDescription(replaceUmlaut("Zobrazit informace o verzi."))
                .create());

        options.addOption(OptionBuilder
                .withDescription(replaceUmlaut("Akce, která má být provedena. Povolené hodnoty jsou BUILD_MINIFIED_PACKAGE a GENERATE_JWT_KEYS."))
                .hasArg()
                .withArgName("AKCE")
                .withLongOpt(Params.ACTION)
                .create("a"));

        options.addOption(OptionBuilder
                .withDescription(replaceUmlaut(
                        "Adresář, nebo soubor ZIP obsahující PSP balík. " +
                                "Parametr je povinný pro akci BUILD_MINIFIED_PACKAGE."))
                .hasArg()
                .withArgName("ADRESAR/SOUBOR_ZIP")
                .withLongOpt(Params.PSP)
                .create());

        options.addOption(OptionBuilder
                .withDescription(replaceUmlaut(
                        "Adresář, kam bude bude uložen minifikovaný PSP balík. " +
                                "Parametr je povinný pro akci BUILD_MINIFIED_PACKAGE."))
                .hasArg()
                .withArgName("ADRESAR_PRO_MINIFIKOVANY_PSP")
                .withLongOpt(Params.MINIFIED_PSP_DIR)
                .create());

        CommandLineParser parser = new DefaultParser();
        try {
            // System.out.println(toString(args));
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);
            if (line.hasOption(Params.HELP)) {
                printHelp(options);
            } else if (line.hasOption(Params.VERSION)) {
                Version version = new Version();
                System.out.println(String.format("Komplexni-validator-web  CLI verze %s, sestaveno: %s", version.version, version.build));
            } else {

                //action
                if (!line.hasOption(Params.ACTION)) {
                    System.err.println(String.format("Chyba: prázdný parametr %s!", Params.ACTION));
                    printHelp(options);
                    return;
                }
                String actionStr = line.getOptionValue(Params.ACTION);
                Action action;
                try {
                    action = Action.valueOf(actionStr);
                } catch (java.lang.IllegalArgumentException e) {
                    System.err.println(String.format("Chyba: neznámá akce %s!", actionStr));
                    printHelp(options);
                    return;
                }


                //psp / psp-group
                File psp = null;
                File pspGroup = null;
                switch (action) {
                    case BUILD_MINIFIED_PACKAGE: {
                        if (!line.hasOption(Params.PSP)) {
                            System.err.println(String.format("Chyba: pro akci %s je parametr --%s povinný!", action, Params.PSP));
                            printHelp(options);
                            return;
                        } else {
                            psp = new File(line.getOptionValue(Params.PSP));
                            break;
                        }
                    }
                }

                File minifiedPspDir = null;
                switch (action) {
                    case BUILD_MINIFIED_PACKAGE: {
                        if (!line.hasOption(Params.MINIFIED_PSP_DIR)) {
                            System.err.println(String.format("Chyba: pro akci %s je parametr --%s povinný!", action, Params.MINIFIED_PSP_DIR));
                            printHelp(options);
                            return;
                        } else {
                            minifiedPspDir = new File(line.getOptionValue(Params.MINIFIED_PSP_DIR));
                            break;
                        }
                    }
                    case GENERATE_JWT_KEYS: {
                        try {
                            KeyBuilder keyBuilder = new KeyBuilder();
                            String[] keys = keyBuilder.buildPublicPrivateKey();
                            System.out.println("publicKey: \n" + keys[0]);
                            System.out.println("privateKey: \n" + keys[1]);
                        } catch (Exception e) {
                            System.out.println("Chyba při generování klíčů: " + e.getMessage());
                        }
                        break;
                    }
                }

                switch (action) {
                    case BUILD_MINIFIED_PACKAGE:
                        //TODO: level validace do CLI
                        buildMinifiedPackage(psp, minifiedPspDir, 3);
                        break;
                }
            }
        } catch (ParseException exp) {
            System.err.println("Chyba parsování parametrů: " + exp.getMessage());
            printHelp(options);
        }
    }

    /*Docasne odstrani diakritiku, dokud se neopravi problem s kodovanim na Windows*/
    private static String replaceUmlaut(String string) {
        Map<String, String> replacements = new HashMap<>();
        replacements.put("á", "a");
        replacements.put("Á", "A");
        replacements.put("é", "e");
        replacements.put("É", "E");
        replacements.put("ě", "e");
        replacements.put("Ě", "E");
        replacements.put("í", "i");
        replacements.put("Í", "I");
        replacements.put("ó", "o");
        replacements.put("Ó", "O");
        replacements.put("ů", "u");
        replacements.put("Ů", "U");
        replacements.put("ú", "u");
        replacements.put("Ú", "U");

        replacements.put("č", "c");
        replacements.put("Č", "C");
        replacements.put("ď", "d");
        replacements.put("Ď", "D");
        replacements.put("ň", "n");
        replacements.put("Ň", "N");
        replacements.put("ř", "r");
        replacements.put("Ř", "R");
        replacements.put("š", "s");
        replacements.put("Š", "S");
        replacements.put("ť", "t");
        replacements.put("Ť", "T");
        replacements.put("ž", "z");
        replacements.put("Ž", "Z");

        for (String letter : replacements.keySet()) {
            string = string.replaceAll(letter, replacements.get(letter));
        }
        return string;
    }

    private static void buildMinifiedPackage(File pspDirOrZipFile, File minifiedPspDir, int level) {
        System.out.println("Vyrábím minifikovaný psp balík ze zdrojového balíku " + pspDirOrZipFile.getAbsolutePath());
        System.out.println("Výsledek bude uložen do adresáře " + minifiedPspDir.getAbsolutePath());
        //checkou minifiedPspDir
        if (!minifiedPspDir.exists()) {
            System.err.println(String.format("Chyba: adresář %s neexistuje!", minifiedPspDir.getAbsolutePath()));
        } else if (!minifiedPspDir.isDirectory()) {
            System.err.println(String.format("Chyba: soubor %s není adresář!", minifiedPspDir.getAbsolutePath()));
        } else if (!minifiedPspDir.canWrite()) {
            System.err.println(String.format("Chyba: nemůžu zapisovat do adresáře %s!", minifiedPspDir.getAbsolutePath()));
        } else if (!pspDirOrZipFile.exists()) {
            throw new IllegalStateException(String.format("Soubor %s neexistuje", pspDirOrZipFile.getAbsolutePath()));
        } else {
            try {
                if (pspDirOrZipFile.isDirectory()) {
                    if (!pspDirOrZipFile.canRead()) {
                        throw new IllegalStateException(String.format("Nelze číst adresář %s", pspDirOrZipFile.getAbsolutePath()));
                    }
                    File minifiedDir = minifyPspDir(pspDirOrZipFile, minifiedPspDir, level);
                    Utils.zipFolder(minifiedDir.getAbsolutePath(), minifiedDir.getAbsolutePath().replace("_extracted", "") + ".zip");
                    Utils.deleteNonemptyDir(minifiedDir);
                } else {
                    File minifiedDir = minifyPspZip(pspDirOrZipFile, minifiedPspDir, minifiedPspDir, level);
                    Utils.zipFolder(minifiedDir.getAbsolutePath(), minifiedDir.getAbsolutePath().replace("_extracted", "") + ".zip");
                    Utils.deleteNonemptyDir(minifiedDir);
                }
            } catch (IOException e) {
                throw new IllegalStateException("Chyba při minifikaci ", e);
            }
        }
    }

    private static File minifyPspZip(File pspZipFile, File tmpRootDir, File minifiedRootDir, int level) throws IOException {
        try {
            try {
                new ZipFile(pspZipFile);
            } catch (ZipException e) {
                System.out.println(String.format("Soubor %s není adresář ani soubor ZIP, ignoruji.", pspZipFile.getAbsolutePath()));
            }
            if (!tmpRootDir.exists()) {
                System.err.println(String.format("Chyba: adresář %s neexistuje!", pspZipFile.getAbsolutePath()));
            } else if (!tmpRootDir.isDirectory()) {
                System.err.println(String.format("Chyba: soubor %s není adresář!", pspZipFile.getAbsolutePath()));
            } else if (!tmpRootDir.canWrite()) {
                System.err.println(String.format("Chyba: nemůžu zapisovat do adresáře %s!", pspZipFile.getAbsolutePath()));
            } else {
                File tmpPspDir = new File(tmpRootDir, pspZipFile.getName().replace(".zip", "") + "_extracted");
                if (tmpPspDir.exists()) {
                    System.out.println(String.format("Mažu adresář %s", tmpPspDir.getAbsolutePath()));
                    Utils.deleteNonemptyDir(tmpPspDir);
                }
                System.out.println(String.format("Rozbaluji %s do adresáře %s", pspZipFile.getAbsolutePath(), tmpPspDir.getAbsolutePath()));
                Utils.unzip(pspZipFile, tmpPspDir);
                File minifiedDir = minifyPspDir(tmpPspDir, minifiedRootDir, level);
                Utils.deleteNonemptyDir(tmpPspDir);
                return minifiedDir;
            }
        } catch (IOException e) {
            System.out.println(String.format("Chyba zpracování ZIP souboru %s: %s!", pspZipFile.getAbsolutePath(), e.getMessage()));
        }
        return null;
    }

    /**
     * @param level uroven minifikace: 1: obrazky + audio, 2: obrazky + audio + txt, 3: obrazky + audio + txt + alto
     */
    private static File minifyPspDir(File pspDir, File minifiedRootDir, int level) throws IOException {
        String newDirName = pspDir.getName() + "_minified";
        File minifiedDir = new File(minifiedRootDir, newDirName);
        if (minifiedDir.exists()) {
            System.out.println("Adresář existuje, mažu: " + minifiedDir.getAbsolutePath());
            Utils.deleteNonemptyDir(minifiedDir);
        }
        Files.walkFileTree(pspDir.toPath(), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path newDir = minifiedDir.toPath().resolve(pspDir.toPath().relativize(dir));
                System.out.println("Vytvářím adresář: " + newDir);
                Files.createDirectory(newDir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                List<String> suffixesOfFilesToMinify;
                if (level == 1) {
                    suffixesOfFilesToMinify = Arrays.asList(".jp2", ".wav", ".mp3");
                } else {
                    suffixesOfFilesToMinify = Arrays.asList(".jp2", ".wav", ".mp3", ".txt");
                }
                boolean minify = false;
                for (String suffix : suffixesOfFilesToMinify) {
                    if (file.getFileName().toString().endsWith(suffix)) {
                        minify = true;
                        break;
                    }
                }
                if (level >= 3) {
                    if (file.getFileName().toString().contains("alto")) {
                        minify = true;
                    }
                }
                if (minify) {
                    System.out.println("Minifikuji soubor: " + file);
                    Path newFile = minifiedDir.toPath().resolve(pspDir.toPath().relativize(file));
                    Files.createFile(newFile);
                } else {
                    Path newFile = minifiedDir.toPath().resolve(pspDir.toPath().relativize(file));
                    System.out.println("Kopíruji soubor: " + newFile);
                    Files.copy(file, newFile);
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return minifiedDir;
    }

    private static void printHelp(Options options) {
        String header = replaceUmlaut("\n" +
                "CLI pro podporu Webové verze Komplexního validátoru\n" +
                "===================================================\n" +
                "\n" +
                "Generování JWT klíčů:\n" +
                "----------------------------\n" +
                "Bude vygenerována dvojice klíčů (soukromý a veřejný), které budou sloužit k podpisování a ověřování podpisů JWT tokenů vydávaných lokálně.\n" +
                "Ty slouží k autentizaci vzájemného volání služeb mezi sebou.\n" +
                "Vygenerované klíče je potřeba vložit do konfiguračního souboru (hodnoty jwt.local.private-key a jwt.local.public-key)\n" +
                "\n" +
                "Výroba minifikovaného balíku:\n" +
                "----------------------------\n" +
                "Bude vyrobena kopie balíku s tím, že textové a obrazové soubory budou nahrazny prázdnými soubory se stejným jménem.\n" +
                "\n");
        String footer = replaceUmlaut("\n*Definice metadatových formátů. Více na http://www.ndk.cz/standardy-digitalizace/metadata.\n" +
                "Více informací o validátoru najdete na https://github.com/NLCR/komplexni-validator.");
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(100);
        formatter.setOptionComparator(null);
        formatter.printHelp("java -jar KomplexniValidatorWebCLI.jar", header, options, footer, true);
    }
}