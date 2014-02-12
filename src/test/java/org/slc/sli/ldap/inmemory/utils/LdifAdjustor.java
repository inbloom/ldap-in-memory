package org.slc.sli.ldap.inmemory.utils;


import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.*;
import java.util.*;

/**
 * This Inbloom-only utility class is used to update an LDIF export file that is generated from Apache Directory studio,
 * so it will import via the UnboundedId SDK into an in-memory server (which uses an openLDAP schema).
 *
 * Feel free to adapt or modify as needed for your environment, but be aware this is intended to be a focused tool for
 * internal use and not part of an SDK.
 *
 * Created by tfritz on 12/30/13.
 */
public class LdifAdjustor {

    private static final String INCLUDE_USERS_CONFIG = "include_users.txt";

    private static final String PASSWORD_ENTRY = "userPassword";

    private List<String> ldifFile;

    private boolean FILTER_USERS = true;
    private Set<String> INCLUDE_USERS = new LinkedHashSet<String>();

    public enum CliParams {
        HELP("help"),
        FILE_NAME("f"),
        SOURCE_DIR("s"),
        OUTPUT_DIR("o"),
        DEBUG("debug"),
        SUFFIX("suffix");

        private String name;

        private CliParams(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public enum SectionTypeEnum {
        PEOPLE,
        GROUP,
        OTHER;
    }

    public class Section implements Serializable {
        public String key;
        public List<String> lines = new ArrayList<String>();
    }


    public Options buildOptions() {
        Options options = new Options();

        options.addOption(CliParams.HELP.getName(), null, false, "Show help");

        options.addOption(CliParams.DEBUG.getName(), null, false, "Show additional debug info.");

        options.addOption(OptionBuilder.withLongOpt(CliParams.FILE_NAME.getName())
              .withDescription("The LDIF file name.")
              .hasArg()
              .withArgName("File Name")
              .create());

        options.addOption(OptionBuilder.withLongOpt(CliParams.SOURCE_DIR.getName())
              .withDescription("The fully qualified source directory.")
              .hasArg()
              .withArgName("source directory")
              .create());


        options.addOption(OptionBuilder.withLongOpt(CliParams.OUTPUT_DIR.getName())
              .withDescription("The fully qualified output directory.")
              .hasArg()
              .withArgName("output directory")
              .create());

        options.addOption(OptionBuilder.withLongOpt(CliParams.SUFFIX.getName())
              .withDescription("The suffix to append to the filename when saving to output directory.")
              .hasArg()
              .withArgName("file suffix")
              .create());

        return options;
    }

    public boolean stringContainsUser(final String line) {
        if (StringUtils.isEmpty(line)) {
            return false;
        } else {
            Iterator iter = INCLUDE_USERS.iterator();
            while (iter.hasNext()) {
                String val = (String)iter.next();
                if (line.contains(val)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean stringEndsWithUser(final String line) {
        if (StringUtils.isEmpty(line)) {
            return false;
        } else {
            Iterator iter = INCLUDE_USERS.iterator();
            while (iter.hasNext()) {
                String val = (String)iter.next();
                if (line.endsWith(val)) {
                    return true;
                }
            }
        }
        return false;
    }

    public SectionTypeEnum detectSectionType(final String sectionHeader) {
        if (StringUtils.isEmpty(sectionHeader)) {
            System.out.println("Unable to identify section type");
            //System.exit(0);
            return SectionTypeEnum.OTHER;
        }
        if (sectionHeader.contains("ou=people")) {
            return SectionTypeEnum.PEOPLE;
        }
        if (sectionHeader.contains("ou=groups")) {
            return SectionTypeEnum.GROUP;
        }
        System.out.println("Unable to identify section type: " + sectionHeader);
        return SectionTypeEnum.OTHER;
    }

    /**
     * Parse the LDIF input file into sections.
     * @param inputFile
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public List<String> readLdifIntoList(final String inputFile) throws FileNotFoundException, IOException {
        System.out.println(">>>LdifAdjustor.readLdifIntoList()");
        List<String> output = new LinkedList<String>();

        FileInputStream fstream = new FileInputStream(inputFile);
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line;

        while ((line = br.readLine()) != null) {
            output.add(line);
        }

        System.out.println("<<<LdifAdjustor.readLdifIntoList()");
        return output;
    }

    /**
     * Parse the LDIF input file into sections.
     * @param ldif the ldif file loaded into a List<String>.
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public List<Section> parseLdif(final List<String> ldif) throws FileNotFoundException, IOException {
        System.out.println(">>>LdifAdjustor.parseLdif()");

        List<Section> output = new LinkedList<Section>();

        int fileLineCount = 0;
        int sectionLineCount = 0; //counter to track # of lines
        Section section = new Section();

        boolean hasSection = false; //this boolean is used to skip blank lines at the beginning of an LDIF, should they exist.
        boolean beginNewSection = true; //boolean used to control logic to begin a new section.

        for (String line : ldif) {
            //a blank link indicates that a new section should begin.
            //the blank line will be saved at the end of a section (for when file is created from array).

            if (StringUtils.isEmpty(line)) {

                //empty line detected, which means finish the existing section, add to output, and start a new section.
                //BUT, if the parser has not yet built a section (e.g. beginning of file, then skip these lines.
                if (hasSection) {
                    sectionLineCount++;
                    section.lines.add(line);
                    //save existing section and start a new one.
                    if (StringUtils.isEmpty(section.key)) {
                        section.key = section.lines.get(0);
                        System.out.println("...setting sectionHeader to: " + section.key);
                    }
                    if (StringUtils.isEmpty(section.key)) {
                        System.out.println("ADDED EMPTY LINE AS KEY...");  //you know what this means for a Map... output.put(sectionHeader, new ArrayList(section));
                        System.exit(0); //examine LDIF and fix, or revise this utility.
                    }
                    System.out.println("...Adding section with " + section.lines.size() + " lines.");  //you know what this means for a Map...
                    output.add(section);

                    beginNewSection = true;
                } else {
                    //skip empty lines at start of file.
                    System.out.println("...SKIPPING empty line at start of LDIF...");
                }

            } else {
                //the first non-empty string will fall here, as will additions to an existing section.
                hasSection = true;

                sectionLineCount++;
                fileLineCount++;

                //handle new section first.
                if (beginNewSection) {
                    beginNewSection = false;
                    section = new Section();
                    section.lines.add(line);
                    sectionLineCount = 1;
                    section.key = line;
                } else {
                    section.lines.add(line);
                }

            }
        }

        System.out.println("# of lines in LDIF file:  " + fileLineCount);
        System.out.println("<<<LdifAdjustor.parseLdif()");

        return output;
    }

    /**
     * Output entries to System.out
     * @param sections
     */
    public void showSections(List<Section> sections) {
        System.out.println(">>>LdifAdjustor.showSections()");
        System.out.println("# of entries: " + sections.size());
        for (Section section : sections) {
            for (String ln : section.lines) {
                System.out.println(ln);
            }
        }
        System.out.println("<<<LdifAdjustor.showSections()");
    }

    /**
     * Process groups
     * @param sections
     * @return
     */
    public List<Section> processGroups(List<Section> sections) {
        System.out.println(">>>LdifAdjustor.processGroups()");

        final List<Section> output = new LinkedList<Section>();

        //iterate through each group and perform required
        for (Section section : sections) {
            Section newSection = new Section();
            newSection.key = section.key;
            newSection.lines = section.lines;

            SectionTypeEnum sectionType = detectSectionType(newSection.key);
            if (sectionType == SectionTypeEnum.GROUP) {
                System.out.println(newSection.key);
                System.out.println("   " + sectionType);
                //remove memberUid entry if it does not contain one of the users specified in the white list
                System.out.println("   lines: " + section.lines.size());

                final List<String> newLines = new LinkedList<String>();

                for (String s : section.lines) {
                    if (StringUtils.isEmpty(s)) {
                        newLines.add(s);
                    } else if (s.startsWith("memberUid:")) {
                        if (stringContainsUser(s)) {
                           newLines.add(s);
                        } else {
                            System.out.println("Removing group memberUid entry: " + s);
                        }
                    } else {
                        newLines.add(s);
                    }

                }

                newSection.lines = newLines;

            }

            System.out.println("   newSection lines: " + section.lines.size());
            output.add(newSection);
        }

        for (Section section : output) {
            System.out.println("key: " + section.key);
            System.out.println("   # of lines: " + section.lines.size());
        }

        System.out.println("<<<LdifAdjustor.processGroups()");

        return output;
    }

    /**
     * Process Person sections
     * @param sections
     * @return
     */
    public List<Section> processPeople(List<Section> sections) {
        List<Section> output = new LinkedList<Section>();

        System.out.println(">>>LdifAdjustor.processPeople()");
        //iterate through each person and perform required
        for (Section section : sections) {

            SectionTypeEnum sectionType = detectSectionType(section.key);
            if (sectionType == SectionTypeEnum.PEOPLE) {
                System.out.println(section.key);
                System.out.println("   " + sectionType);

                //if this section does not belong to a whitelist user then remove it.
                boolean removeUser = false;
                System.out.println("   lines: " + section.lines.size());

                for (String s : section.lines) {
                    if (!StringUtils.isEmpty(s) && s.startsWith("uid: ")) {
                        removeUser = true;
                        System.out.println(" uid -- " + s);
                        if (stringContainsUser(s)) {
                            if (stringEndsWithUser(s)) {
                                System.out.println("...keep user: " + s);
                                removeUser = false;
                            }
                        }
                    }
                }

                if (removeUser) {
                    System.out.println("Excluding person section for user: " + section.key);
                } else {
                    output.add(section);
                }
            } else {
                //keep unidentified sections; this code adds them.
                output.add(section);
            }

        }

        System.out.println("<<<LdifAdjustor.processPeople()");

        return output;
    }

    /**
     * Process Person sections
     * @param sections
     * @return
     */
    public List<Section> processAllSections(List<Section> sections) {
        List<Section> output = new LinkedList<Section>();

        System.out.println(">>>LdifAdjustor.processAllSections()");
        //iterate through all sections and section items
        for (Section section : sections) {
            Section newSection = new Section();
            newSection.key = section.key;

            boolean containsDN = false;
            boolean addedChangeType = false;

            for (String s : section.lines) {
                if (s.startsWith("dn: ")) {
                    containsDN = true;
                }
                //exclude entryCSN lines
                if (s.startsWith("entryCSN") ) { //&& !strLine.startsWith("hasSubordinates")) {
                    System.out.println("skipping entryCSN...");
                } else {
                    //changetype needs to be 2nd entry after dn, which can span multiple lines but usually
                    //preceedes the objectClass.  The rules are the section MUST begin with DN.
                    if (s.startsWith("objectClass:") && containsDN && !addedChangeType) {
                        newSection.lines.add("changetype: add");
                        addedChangeType = true;
                    }
                    newSection.lines.add(s);
                }
            }

            output.add(newSection);

        }

        System.out.println("<<<LdifAdjustor.processAllSections()");

        return output;
    }

    /**
     * This method iterates through all People nodes and firstly finds the password entry
     * for a specific user.  It then iterates through all People nodes and replaces any
     * existing userpassword entry with the target user.  This allows LDIF exports to "hide"
     * encrypted password values with a single value.
     * NOTE:  It is assumed the userPassword value will be last, so following lines that begin with
     * a single space will be removed.
     * @param sections
     * @param userPasswordLine The user password line to use.
     * @return
     */
    public List<Section> replaceUserPasswordEntries(List<Section> sections, String userPasswordLine) {
        List<Section> output = new LinkedList<Section>();

        if (StringUtils.isEmpty(userPasswordLine)) {
            return sections;
        }

        System.out.println(">>>LdifAdjustor.replaceUserPasswordEntries()");

        //iterate through all sections and section items
        for (Section section : sections) {
            Section newSection = new Section();
            newSection.key = section.key;

            //indicator to track when pw line is found (use recursion in future if more robust impl needed).
            boolean currentAttributeIsUserPassword = false;

            for (String s : section.lines) {
                //exclude entryCSN lines
                if (s.startsWith(LdifAdjustor.PASSWORD_ENTRY)) {
                    newSection.lines.add(userPasswordLine);
                    currentAttributeIsUserPassword = true;
                } else if (currentAttributeIsUserPassword) {
                   if (s.startsWith(" ")) {
                        System.out.println("   removing line wrap for userPassword");
                   } else {
                       currentAttributeIsUserPassword = false;
                       newSection.lines.add(s);
                   }
                } else {
                    newSection.lines.add(s);
                }
            }

            output.add(newSection);

        }

        System.out.println("<<<LdifAdjustor.replaceUserPasswordEntries()");

        return output;
    }

    /**
     * Iterates through people nodes to find the
     * @param sections
     * @param uid
     * @return
     */
    public String findPasswordForUser(List<Section> sections, String uid) {
        String userPasswordLine = null;

        System.out.println(">>>LdifAdjustor.findPasswordForUser()");
        //iterate through each person and perform required
        for (Section section : sections) {

            SectionTypeEnum sectionType = detectSectionType(section.key);
            if (sectionType == SectionTypeEnum.PEOPLE) {
                userPasswordLine = null;
                boolean foundUser = false;
                boolean foundUserPassword = false;

                for (String s : section.lines) {
                    //uid should be before password, but in case it is not check for them individually.
                    if (!StringUtils.isEmpty(s) && s.startsWith("uid: ")) {
                        if (s.endsWith(uid)) {
                            System.out.println("   Found uid for: " + uid);
                            foundUser = true;
                        }
                    }

                    if (!StringUtils.isEmpty(s) && s.startsWith(LdifAdjustor.PASSWORD_ENTRY)) {
                        userPasswordLine = s;
                        foundUserPassword = true;
                    }
                }

                if (foundUser) {
                    System.out.println("   Found entry for user: " + uid);
                    if (foundUserPassword) {
                        System.out.println("   Found password for user: " + userPasswordLine);
                        return userPasswordLine;
                    }
                }
            }

        }

        System.out.println("   userPassword not found for uid: " + uid);
        System.out.println("<<<LdifAdjustor.findPasswordForUser()");
        return userPasswordLine;
    }

    /**
     * Runs the utility and parses input to output a modified LDIF export file.
     * Add --debug to spool output file contents to console.
     * --help
     * --f "ciDevLDIF.ldif" --s "/Users/tfritz/Projects/datastore_ldap_inmemory/src/test/resources/ldif/original" --o "/Users/tfritz/Projects/datastore_ldap_inmemory/src/test/resources/ldif" --suffix "modified"
     * @param
     */
    public static void main(String[] args) {
        System.out.println(">>>Starting main()");
        LdifAdjustor impl = new LdifAdjustor();
        impl.run(args);
        System.out.println("<<<Starting main()");
    }

    /**
     * Implementation for an instance of the LDIF adjustor.
     * //TODO parameterize features as need arises for other uses (at this time it's all required).
     * @param args
     */
    public void run(String[] args) {
        System.out.println(">>>Starting LdifAdjustor()");

        final String defaultSuffix = ".modified";

        boolean debugEnabled = false;
        boolean changePassword = false;

        CommandLineParser parser = new BasicParser();

        List<Section> parsedLdifFile = null;

        try {
            // parse the command line arguments
            Options options = buildOptions();
            CommandLine line = parser.parse(options, args);

            /* If help switch is provided, show help and exit. */
            if(line.hasOption(CliParams.HELP.getName())) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp(LdifAdjustor.class.getName(), options);
                return;
            }

            if (line.hasOption(CliParams.DEBUG.getName())) {
                debugEnabled = true;
            }

            final String fileName = line.getOptionValue(CliParams.FILE_NAME.getName());
            final String sourcePath = line.getOptionValue(CliParams.SOURCE_DIR.getName());
            final String outputPath = line.getOptionValue(CliParams.OUTPUT_DIR.getName());
            String suffix = defaultSuffix;
            if (line.hasOption(CliParams.SUFFIX.getName())) {
                suffix = line.getOptionValue(CliParams.SUFFIX.getName());
            }

            final String inputFile = sourcePath + File.separator + fileName;
            final String outputFile = outputPath + File.separator + fileName + "." + suffix;

            System.out.println("inputFile: " + inputFile);
            System.out.println("outputFile: " + outputFile);

            if (FILTER_USERS) {
                INCLUDE_USERS = LdifAdjustor.readUserIncludeConfig(sourcePath + File.separator + LdifAdjustor.INCLUDE_USERS_CONFIG);
                for (String user : INCLUDE_USERS) {
                    System.out.println("   including user: " + user);
                }
            }

            /* Load the LDIF file into a String array. */
            ldifFile = readLdifIntoList(inputFile);

            //Parse the LDIF into sections.
            parsedLdifFile = parseLdif(this.ldifFile);

            if (debugEnabled) {
                showSections(parsedLdifFile);
            }

            //Perform group level processing
            parsedLdifFile = processGroups(parsedLdifFile);
            System.out.println("# of sections after group processing: " + parsedLdifFile.size());

            //perform person(people node) level processing.
            //TODO improve impl to register observers to process each line item accordingly.
            parsedLdifFile = processPeople(parsedLdifFile);
            System.out.println("# of sections after people processing: " + parsedLdifFile.size());

            //perform processing across all sections.
            parsedLdifFile = processAllSections(parsedLdifFile);

            //perform password processing, which replaces existing userPassword values within an LDIF
            //with that from a common user.  Note:  The FIRST occurrence of the user that is found is what
            //will be used.
            //TODO parameterize the target user.
            String uid = "linda.kim";
            String userPasswordLine = this.findPasswordForUser(parsedLdifFile, uid);
            //iterate through people sections and replace the userPasswordLine (if it is not null)
            parsedLdifFile = this.replaceUserPasswordEntries(parsedLdifFile, userPasswordLine);

            System.out.println("writing output file..." + outputFile);
            try {
                saveFile(outputFile, parsedLdifFile);
                System.out.println("...completed");
            } catch (FileNotFoundException e) {
                System.out.println(ExceptionUtils.getStackTrace(e));
            }

        } catch (Exception e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }

        System.out.println("<<<Starting LdifAdjustor()");

    }

    /**
     * Reads external file that contains a list of usernames, on per line, to filter ldap entries on.
     * @param name
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static Set<String> readUserIncludeConfig(final String name) throws FileNotFoundException, IOException {
        Set<String> usernames = new LinkedHashSet<String>(255);
        FileInputStream fstream = new FileInputStream(name);  //throws FileNotFoundException
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line;

        while ((line = br.readLine()) != null) {
            usernames.add(line);
        }

        in.close();
        return usernames;
    }


    public void saveFile(String fileName, List<Section> sections) throws FileNotFoundException {
        System.out.println(">>>LdifAdjustor.saveFile()");

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));

            for (Section section : sections) {
                for (String s : section.lines) {
                    writer.write(s);
                    writer.newLine();
                    writer.flush();
                }
            }
            writer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        System.out.println("<<<LdifAdjustor.saveFile()");
    }

}
