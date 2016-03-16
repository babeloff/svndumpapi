package com.github.cstroe.svndumpgui.api;

import com.github.cstroe.svndumpgui.internal.ContentChunkImpl;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

import java.util.Arrays;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ContentChunkImplSteps {

    private byte[] byteArray;
    private Supplier<ContentChunk> chunkConstructor;
    private Runnable delayedRunnable;
    private ContentChunk contentChunk;
    private ContentChunk newContentChunk;

    @Given("a null byte array")
    public void aNullByteArray() {
        byteArray = null;
    }

    @Given("a ContentChunkImpl with the content \"$content\"")
    public void aContentChunk(byte[] content) {
        contentChunk = new ContentChunkImpl(content);
    }

    @Given("a ContentChunk that returns null content")
    public void defineABadContentChunkImplementation() {
        contentChunk = new ContentChunk() {
            @Override
            public byte[] getContent() {
                return null;
            }

            @Override
            public void setContent(byte[] content) {}
        };
    }

    @When("set the content of the new copy to \"$content\"")
    public void setNewContentChunkContent(byte[] content) {
        newContentChunk = chunkConstructor.get();
        byte[] oldContent = newContentChunk.getContent();
        System.arraycopy(content, 0, oldContent, 0, content.length);
    }

    @When("we pass it to the constructor of ContentChunkImpl")
    public void defineAConstructorRunnable() {
        chunkConstructor = () -> new ContentChunkImpl(byteArray);
        delayedRunnable = () -> chunkConstructor.get();
    }

    @When("we pass it to the copy constructor of ContentChunkImpl")
    public void defineACopyConstructorRunnable() {
        chunkConstructor = () -> new ContentChunkImpl(contentChunk);
        delayedRunnable = () -> chunkConstructor.get();
    }

    @Then("the original ContentChunkImpl should still contain the content \"$content\"")
    public void checkOriginalContent(byte[] content) {
        assertTrue(Arrays.equals(content, contentChunk.getContent()));
    }

    @Then("the new ContentChunkImpl should contain the content \"$content\"")
    public void checkNewContent(byte[] content) {
        assertTrue(Arrays.equals(content, newContentChunk.getContent()));
    }

    @When("we instantiate a ContentChunkImpl with null content")
    public void chunkWithContent() {
        delayedRunnable = () -> new ContentChunkImpl((byte[])null);
    }

    @Then("it should throw an $exception")
    public void shouldThrowException(String exception) throws ClassNotFoundException {
        Class<?> exceptionClass = Class.forName(exception);
        try {
            delayedRunnable.run();
        } catch (Exception ex) {
            assertEquals(exceptionClass, ex.getClass());
            return;
        }
        fail("Expected exception " + exception + ", but was never thrown.");
    }
}
