package com.github.cstroe.svndumpgui.internal.transform.replace;

import com.github.cstroe.svndumpgui.api.ContentChunk;
import com.github.cstroe.svndumpgui.api.Node;
import com.github.cstroe.svndumpgui.api.NodeHeader;
import com.github.cstroe.svndumpgui.api.RepositoryWriter;
import com.github.cstroe.svndumpgui.api.Revision;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.generated.SvnDumpParser;
import com.github.cstroe.svndumpgui.internal.ContentChunkImpl;
import com.github.cstroe.svndumpgui.internal.NodeImpl;
import com.github.cstroe.svndumpgui.internal.RevisionImpl;
import com.github.cstroe.svndumpgui.internal.utility.Md5;
import com.github.cstroe.svndumpgui.internal.utility.Sha1;
import com.github.cstroe.svndumpgui.internal.utility.TestUtil;
import com.github.cstroe.svndumpgui.internal.writer.RepositoryInMemory;
import com.github.cstroe.svndumpgui.internal.writer.SvnDumpWriter;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class FileContentReplaceTest {
    @Test
    public void simple_replace() throws ParseException, NoSuchAlgorithmException {
        final String newFileContent = "No content.\n";

        Predicate<Node> predicate = n -> n.getRevision().get().getNumber() == 1 && n.getHeaders().get(NodeHeader.PATH).equals("README.txt");
        FileContentReplace fileContentReplace = new FileContentReplace(predicate, n -> new ContentChunkImpl(newFileContent.getBytes()));

        RepositoryInMemory inMemory = new RepositoryInMemory();
        fileContentReplace.continueTo(inMemory);

        SvnDumpParser.consume(TestUtil.openResource("dumps/add_file.dump"), fileContentReplace);

        assertThat(inMemory.getRepo().getRevisions().size(), is(2));
        Revision r1 = inMemory.getRepo().getRevisions().get(1);
        assertThat(r1.getNodes().size(), is(1));
        Node node = r1.getNodes().get(0);
        assertThat(new String(node.getContent().get(0).getContent()), is(equalTo(newFileContent)));
        assertThat(node.get(NodeHeader.TEXT_DELTA), is(equalTo("true")));
        assertThat(node.get(NodeHeader.TEXT_CONTENT_LENGTH), is(equalTo("12")));
        assertThat(node.get(NodeHeader.CONTENT_LENGTH), is(equalTo("22")));
        assertThat(node.get(NodeHeader.MD5), is(equalTo(new Md5().hash(newFileContent.getBytes()))));
        assertThat(node.get(NodeHeader.SHA1), is(equalTo(new Sha1().hash(newFileContent.getBytes()))));
    }

    @Test
    public void if_no_match_then_no_change() throws ParseException, IOException {
        Predicate<Node> predicate = n -> false;
        FileContentReplace fileContentReplace = new FileContentReplace(predicate, n -> null);

        ByteArrayOutputStream newDump = new ByteArrayOutputStream();
        RepositoryWriter svnDumpWriter = new SvnDumpWriter();
        svnDumpWriter.writeTo(newDump);

        fileContentReplace.continueTo(svnDumpWriter);

        SvnDumpParser.consume(TestUtil.openResource("dumps/add_file.dump"), fileContentReplace);

        TestUtil.assertEqualStreams(TestUtil.openResource("dumps/add_file.dump"), new ByteArrayInputStream(newDump.toByteArray()));
    }

    @Test(expected = NullPointerException.class)
    public void does_not_allow_null_chunk() throws ParseException {
        Predicate<Node> predicate = n -> true;
        FileContentReplace fileContentReplace = new FileContentReplace(predicate, n -> null);

        ByteArrayOutputStream newDump = new ByteArrayOutputStream();
        RepositoryWriter svnDumpWriter = new SvnDumpWriter();
        svnDumpWriter.writeTo(newDump);

        fileContentReplace.continueTo(svnDumpWriter);

        SvnDumpParser.consume(TestUtil.openResource("dumps/add_file.dump"), fileContentReplace);
    }

    @Test
    public void tracks_copied_file_across_one_copy() throws ParseException, IOException {
        Predicate<Node> nodeMatcher = n -> n.getRevision().get().getNumber() == 1 && "README.txt".equals(n.get(NodeHeader.PATH));
        FileContentReplace fileContentReplace = new FileContentReplace(nodeMatcher, n -> new ContentChunkImpl("new content\n".getBytes()));

        ByteArrayOutputStream newDumpStream = new ByteArrayOutputStream();
        RepositoryWriter svnDumpWriter = new SvnDumpWriter();
        svnDumpWriter.writeTo(newDumpStream);

        fileContentReplace.continueTo(svnDumpWriter);

        SvnDumpParser.consume(TestUtil.openResource("dumps/svn_copy_file.dump"), fileContentReplace);

        TestUtil.assertEqualStreams(TestUtil.openResource("dumps/svn_copy_file_new_content.dump"), new ByteArrayInputStream(newDumpStream.toByteArray()));
    }

    @Test
    public void tracks_copied_file_across_many_copies() throws ParseException, IOException {
        FileContentReplace fileContentReplace =
                FileContentReplace.createFCR(1, "add", "README.txt",
                        n -> new ContentChunkImpl("new content\n".getBytes()));

        ByteArrayOutputStream newDumpStream = new ByteArrayOutputStream();
        RepositoryWriter svnDumpWriter = new SvnDumpWriter();
        svnDumpWriter.writeTo(newDumpStream);

        fileContentReplace.continueTo(svnDumpWriter);

        SvnDumpParser.consume(TestUtil.openResource("dumps/svn_copy_file_many_times.dump"), fileContentReplace);

        TestUtil.assertEqualStreams(TestUtil.openResource("dumps/svn_copy_file_many_times_new_content.dump"), new ByteArrayInputStream(newDumpStream.toByteArray()));
    }

    @Test
    public void nodematch() {
        Predicate<Node> nodeMatch = FileContentReplace.nodeMatch(2, "add", "/somepath/is/here.txt");
        {
            Node ourNode = new NodeImpl(new RevisionImpl(2));
            ourNode.getHeaders().put(NodeHeader.ACTION, "add");
            ourNode.getHeaders().put(NodeHeader.PATH, "/somepath/is/here.txt");

            assertThat(nodeMatch.test(ourNode), is(true));
        }{
            Node ourNode = new NodeImpl(new RevisionImpl(2));
            ourNode.getHeaders().put(NodeHeader.ACTION, "add");
            ourNode.getHeaders().put(NodeHeader.PATH, "/somepath/is/here1.txt");

            assertThat(nodeMatch.test(ourNode), is(false));
        }{
            Node ourNode = new NodeImpl(new RevisionImpl(2));
            ourNode.getHeaders().put(NodeHeader.ACTION, "remove");
            ourNode.getHeaders().put(NodeHeader.PATH, "/somepath/is/here.txt");

            assertThat(nodeMatch.test(ourNode), is(false));
        }{
            Node ourNode = new NodeImpl(new RevisionImpl(3));
            ourNode.getHeaders().put(NodeHeader.ACTION, "add");
            ourNode.getHeaders().put(NodeHeader.PATH, "/somepath/is/here.txt");

            assertThat(nodeMatch.test(ourNode), is(false));
        }
    }

    @Test
    public void chunkFromString() {
        Function<Node, ContentChunk> chunkGenerator = FileContentReplace.chunkFromString("Test chunk.");

        Node ourNode = new NodeImpl(new RevisionImpl(2));
        ourNode.getHeaders().put(NodeHeader.ACTION, "add");
        ourNode.getHeaders().put(NodeHeader.PATH, "/somepath/is/here.txt");

        assertThat(new String(chunkGenerator.apply(ourNode).getContent()), is(equalTo("Test chunk.")));
    }

    @Test
    public void tracks_files_across_deletes() throws ParseException, IOException {
        Predicate<Node> nodeMatcher = n -> n.getRevision().get().getNumber() == 1 && "README.txt".equals(n.get(NodeHeader.PATH));
        FileContentReplace fileContentReplace = new FileContentReplace(nodeMatcher, n -> new ContentChunkImpl("i replaced the content\n".getBytes()));

        ByteArrayOutputStream newDumpStream = new ByteArrayOutputStream();
        RepositoryWriter svnDumpWriter = new SvnDumpWriter();
        svnDumpWriter.writeTo(newDumpStream);

        fileContentReplace.continueTo(svnDumpWriter);

        SvnDumpParser.consume(TestUtil.openResource("dumps/svn_copy_and_delete.before.dump"), fileContentReplace);

        TestUtil.assertEqualStreams(TestUtil.openResource("dumps/svn_copy_and_delete.after.dump"), new ByteArrayInputStream(newDumpStream.toByteArray()));

    }

    @Test
    public void tracks_node_in_directory() throws ParseException, IOException {
        Predicate<Node> nodeMatcher = n -> n.getRevision().get().getNumber() == 2 && "dir1/dir2/dir3/README.txt".equals(n.get(NodeHeader.PATH));
        FileContentReplace fileContentReplace = new FileContentReplace(nodeMatcher, n -> new ContentChunkImpl("new content\n".getBytes()));

        ByteArrayOutputStream newDumpStream = new ByteArrayOutputStream();
        RepositoryWriter svnDumpWriter = new SvnDumpWriter();
        svnDumpWriter.writeTo(newDumpStream);

        fileContentReplace.continueTo(svnDumpWriter);

        SvnDumpParser.consume(TestUtil.openResource("dumps/add_file_in_directory.before.dump"), fileContentReplace);

        InputStream dumpWithNewContent = TestUtil.openResource("dumps/add_file_in_directory.after.dump");
        InputStream dumpCreatedByFileContentReplace = new ByteArrayInputStream(newDumpStream.toByteArray());
        TestUtil.assertEqualStreams(dumpWithNewContent, dumpCreatedByFileContentReplace);
    }

    @Test
    public void tracks_node_in_directory_external_tok() throws ParseException, IOException {
        FileContentReplace fileContentReplace = FileContentReplace.createFCR(
                2, "add", "dir1/dir2/dir3/README.txt", n -> new ContentChunkImpl("new content\n".getBytes()));

        ByteArrayOutputStream newDumpStream = new ByteArrayOutputStream();
        RepositoryWriter svnDumpWriter = new SvnDumpWriter();
        svnDumpWriter.writeTo(newDumpStream);

        fileContentReplace.continueTo(svnDumpWriter);

        SvnDumpParser.consume(TestUtil.openResource("dumps/add_file_in_directory.before.dump"), fileContentReplace);

        InputStream dumpWithNewContent = TestUtil.openResource("dumps/add_file_in_directory.after.dump");
        InputStream dumpCreatedByFileContentReplace = new ByteArrayInputStream(newDumpStream.toByteArray());
        TestUtil.assertEqualStreams(dumpWithNewContent, dumpCreatedByFileContentReplace);
    }

    @Test
    public void does_not_change_a_copy_and_changed_file() throws ParseException, IOException {
        Predicate<Node> nodeMatcher = n -> false;
        FileContentReplace fileContentReplace = new FileContentReplace(nodeMatcher, n -> new ContentChunkImpl("i replaced the content\n".getBytes()));

        ByteArrayOutputStream newDumpStream = new ByteArrayOutputStream();
        RepositoryWriter svnDumpWriter = new SvnDumpWriter();
        svnDumpWriter.writeTo(newDumpStream);

        fileContentReplace.continueTo(svnDumpWriter);

        SvnDumpParser.consume(TestUtil.openResource("dumps/add_and_copychange.dump"), fileContentReplace);

        TestUtil.assertEqualStreams(
                TestUtil.openResource("dumps/add_and_copychange.dump"),
                new ByteArrayInputStream(newDumpStream.toByteArray()));
    }

    @Test
    public void does_not_change_a_change_and_copied_file() throws ParseException, IOException {
        Predicate<Node> nodeMatcher = n -> false;
        FileContentReplace fileContentReplace = new FileContentReplace(nodeMatcher, n -> new ContentChunkImpl("i replaced the content\n".getBytes()));

        ByteArrayOutputStream newDumpStream = new ByteArrayOutputStream();
        RepositoryWriter svnDumpWriter = new SvnDumpWriter();
        svnDumpWriter.writeTo(newDumpStream);

        fileContentReplace.continueTo(svnDumpWriter);

        SvnDumpParser.consume(TestUtil.openResource("dumps/add_and_change_copy_delete.dump"), fileContentReplace);

        TestUtil.assertEqualStreams(
                TestUtil.openResource("dumps/add_and_change_copy_delete.dump"),
                new ByteArrayInputStream(newDumpStream.toByteArray()));
    }

    @Test
    public void handle_multiple_matches_across_simple_copies() throws ParseException, IOException {
        Predicate<Node> nodeMatcher = n -> n.get(NodeHeader.PATH).endsWith("README.txt");
        FileContentReplace fileContentReplace = new FileContentReplace(nodeMatcher, FileContentReplace.chunkFromString("this text is different\n"));

        ByteArrayOutputStream newDumpStream = new ByteArrayOutputStream();
        RepositoryWriter svnDumpWriter = new SvnDumpWriter();
        svnDumpWriter.writeTo(newDumpStream);

        fileContentReplace.continueTo(svnDumpWriter);

        SvnDumpParser.consume(TestUtil.openResource("dumps/simple_copy.dump"), fileContentReplace);

        TestUtil.assertEqualStreams(
                TestUtil.openResource("dumps/simple_copy2.dump"),
                new ByteArrayInputStream(newDumpStream.toByteArray()));
    }
}