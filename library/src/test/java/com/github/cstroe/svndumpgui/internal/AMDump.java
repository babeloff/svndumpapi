package com.github.cstroe.svndumpgui.internal;

import com.github.cstroe.svndumpgui.api.NodeHeader;
import com.github.cstroe.svndumpgui.api.RepositoryConsumer;
import com.github.cstroe.svndumpgui.api.RepositoryValidator;
import com.github.cstroe.svndumpgui.api.RepositoryWriter;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.generated.SvnDumpParser;
import com.github.cstroe.svndumpgui.internal.transform.*;
import com.github.cstroe.svndumpgui.internal.utility.SvnDumpCharStream;
import com.github.cstroe.svndumpgui.internal.validate.PathCollisionValidator;
import com.github.cstroe.svndumpgui.internal.validate.TerminatingValidator;
import com.github.cstroe.svndumpgui.internal.writer.RepositoryAuthors;
import com.github.cstroe.svndumpgui.internal.writer.RepositorySummary;
import com.github.cstroe.svndumpgui.internal.writer.SvnDumpWriter;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class AMDump {

    /**
     * This is a cleanup that I did for the AgreementMaker repository.
     *
     * The original repository was exported to full.dump.
     * Then I filtered large files out with svndumpfilter to get onestep.dump:
     *
     *     cat initial_filter_list.txt > onestep.txt
     *     svndumpfilter exclude --targets initial_filter_list.txt < full.dump > onestep.dump
     *     grep -Poa "Node-path: \K.*\.rdf$" onestep.dump >> onestep.txt
     *     grep -Poa "Node-path: \K.*\.jar$" onestep.dump  >> onestep.txt
     *     grep -Poa "Node-path: \K.*\.zip$" onestep.dump >> onestep.txt
     *     svndumpfilter exclude --targets onestep.txt < full.dump > onestep.dump
     *
     * The code below operates on onestep.dump.
     */

    @Test
    @Ignore
    public void convert_AgreementMaker_repository() throws ParseException, NoSuchAlgorithmException, IOException {

        // add the main branch here
        NodeImpl trunkAgreementMaker = new NodeImpl();
        trunkAgreementMaker.getHeaders().put(NodeHeader.ACTION, "add");
        trunkAgreementMaker.getHeaders().put(NodeHeader.KIND, "dir");
        trunkAgreementMaker.getHeaders().put(NodeHeader.PATH, "trunk/AgreementMaker");
        trunkAgreementMaker.getHeaders().put(NodeHeader.PROP_DELTA, "true");
        trunkAgreementMaker.getHeaders().put(NodeHeader.PROP_CONTENT_LENGTH, "10");
        trunkAgreementMaker.getHeaders().put(NodeHeader.CONTENT_LENGTH, "10");
        trunkAgreementMaker.setProperties(new HashMap<>());

        RepositoryConsumer chain = new NodeAdd(4, trunkAgreementMaker);

        // IM commits
        chain.continueTo(new ClearRevision(1007,1101));

        // AM-Core
        chain.continueTo(new ClearRevision(1));
        chain.continueTo(new ClearRevision(1119));

        // AgreementMaker-tags/AM1
        chain.continueTo(new ClearRevision(1116, 1117));

        // AgreementMakerCVS
        chain.continueTo(new ClearRevision(1850, 1851));

        // workingBranch
        chain.continueTo(new ClearRevision(2037));
        chain.continueTo(new ClearRevision(2041, 2044));
        chain.continueTo(new ClearRevision(2048,2049));
        chain.continueTo(new ClearRevision(2066));
        chain.continueTo(new ClearRevision(2069,2070));

        // Ontologies
        chain.continueTo(new ClearRevision(1847,1848));
        chain.continueTo(new ClearRevision(2031));
        chain.continueTo(new ClearRevision(2134));

        // BSM
        chain.continueTo(new ClearRevision(2161,2162));
        chain.continueTo(new ClearRevision(2169));
        chain.continueTo(new ClearRevision(3069));

        // Matcher-Hierarchy
        chain.continueTo(new ClearRevision(2769,2770));

        // Double AgreementMaker-OSGi
        chain.continueTo(new ClearRevision(3057));
        chain.continueTo(new ClearRevision(3244));

        // Remove double readme
        chain.continueTo(new ClearRevision(434));

        chain.continueTo(new ClearRevision(2346));
        chain.continueTo(new ClearRevision(1199));
        chain.continueTo(new ClearRevision(1567));

        // remove accidental deletion
        // (this was discovered after the release to GitHub, so those commits are still there)
        chain.continueTo(new ClearRevision(2835));
        chain.continueTo(new ClearRevision(2837));

        chain.continueTo(new NodeRemove(440, "add", "trunk/AgreementMaker"));
        chain.continueTo(new NodeRemove(440, "add", "trunk/AgreementMaker/images"));
        chain.continueTo(new NodeRemove(440, "add", "trunk/AgreementMaker/images/aboutImage.gif"));
        chain.continueTo(new NodeRemove(440, "add", "trunk/AgreementMaker/images/advis.png"));
        chain.continueTo(new NodeRemove(440, "add", "trunk/AgreementMaker/images/agreementMaker.png"));
        chain.continueTo(new NodeRemove(440, "add", "trunk/AgreementMaker/images/fileImage.gif"));

        // put everything under trunk
        chain.continueTo(new PathChange("AgreementMaker", "trunk/AgreementMaker"));
        chain.continueTo(new PathChange("NYTInstanceMatcher", "trunk/AgreementMaker/NYTInstanceMatcher"));
        chain.continueTo(new PathChange("MyInstanceMatcher", "trunk/AgreementMaker/MyInstanceMatcher"));
        chain.continueTo(new PathChange("AgreementMaker-SEALSBridge", "trunk/AgreementMaker/AgreementMaker-SEALSBridge"));
        chain.continueTo(new PathChange("AgreementMaker-Matchers", "trunk/AgreementMaker/AgreementMaker-Matchers"));
        chain.continueTo(new PathChange("AM_ROOT", "trunk/AgreementMaker/AM_ROOT"));
        chain.continueTo(new PathChange("AgreementMaker-OSGi", "trunk/AgreementMaker-OSGi"));
        chain.continueTo(new PathChange("AgreementMaker-CollaborationServer", "trunk/AgreementMaker-CollaborationServer"));

        // fix initial history :(
        chain.continueTo(new PathChange("trunk/ScratchPad.txt", "trunk/AgreementMaker/ScratchPad.txt"));
        chain.continueTo(new PathChange("trunk/archives", "trunk/AgreementMaker/archives"));
        chain.continueTo(new PathChange("trunk/ciao", "trunk/AgreementMaker/ciao"));
        chain.continueTo(new PathChange("trunk/images", "trunk/AgreementMaker/images"));
        chain.continueTo(new PathChange("trunk/src", "trunk/AgreementMaker/src"));
        chain.continueTo(new PathChange("trunk/README.txt", "trunk/AgreementMaker/README.txt"));
        chain.continueTo(new PathChange("trunk/AMreminder", "trunk/AgreementMaker/AMreminder"));
        chain.continueTo(new PathChange("trunk/look_and_feel", "trunk/AgreementMaker/look_and_feel"));
        chain.continueTo(new PathChange("trunk/sounds", "trunk/AgreementMaker/sounds"));

        // other fixes, probably came from the svndumpfilter output
        chain.continueTo(new NodeRemove(1843, "add", "branches"));
        chain.continueTo(new NodeRemove(2875, "delete", "trunk/AgreementMaker/AM_ROOT"));

        chain.continueTo(new NodeHeaderChange(2875, "add", "trunk/AgreementMaker-OSGi/AM_ROOT", NodeHeader.COPY_FROM_REV, "2874", "2814"));

        chain.continueTo(new UpdateAuthorForEmptyRevisions("cosmin"));

        RepositoryValidator pathCollisionValidator = new PathCollisionValidator();
        RepositoryValidator terminator = new TerminatingValidator(pathCollisionValidator);
        chain.continueTo(terminator);

        // save the dump
        FileOutputStream fos = new FileOutputStream("/tmp/am_good.dump");
        RepositoryWriter dumpWriter = new SvnDumpWriter();
        dumpWriter.writeTo(fos);
        chain.continueTo(dumpWriter);

        FileOutputStream summaryOs = new FileOutputStream("/tmp/am_good.summary");
        RepositoryWriter summaryWriter = new RepositorySummary();
        summaryWriter.writeTo(summaryOs);
        chain.continueTo(summaryWriter);

        final InputStream s = new FileInputStream("/home/cosmin/Desktop/AgreementMaker-GitHub-Conversion/onestep.dump");
        SvnDumpParser parser = new SvnDumpParser(new SvnDumpCharStream(s));

        parser.Start(chain);

        fos.close();
        summaryOs.close();
    }

    /**
     * I used this method to get a list of authors in the SVN dump file,
     * in order to pass the list to svn2git.
     */
    @Test
    @Ignore
    public void list_authors() throws IOException, ParseException {
        final InputStream s = new FileInputStream("/home/cosmin/Desktop/AgreementMaker-GitHub-Conversion/finished.dump");
        SvnDumpParser parser = new SvnDumpParser(new SvnDumpCharStream(s));
        RepositoryWriter authorsWriter = new RepositoryAuthors();
        authorsWriter.writeTo(System.out);

        parser.Start(authorsWriter);
    }
}
