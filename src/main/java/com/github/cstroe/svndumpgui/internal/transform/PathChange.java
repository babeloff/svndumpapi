package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.Node;
import com.github.cstroe.svndumpgui.api.NodeHeader;
import com.github.cstroe.svndumpgui.api.Property;
import com.github.cstroe.svndumpgui.api.Revision;
import com.github.cstroe.svndumpgui.generated.MergeInfoParser;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.internal.transform.property.MergeInfoData;

public class PathChange extends AbstractRepositoryMutator {

    private final String oldPath;
    private final String newPath;

    public PathChange(String oldPath, String newPath) {
        this.oldPath = oldPath;
        this.newPath = newPath;
    }

    @Override
    public void consume(Revision revision) {
        if(revision.getProperties().containsKey(Property.MERGEINFO)) {
            final String newMergeInfo = updateMergeInfo(revision.getProperties().get(Property.MERGEINFO));
            if(newMergeInfo != null) {
                revision.getProperties().put(Property.MERGEINFO, newMergeInfo);
            }
        }
        super.consume(revision);
    }

    @Override
    public void consume(Node node) {
        final String nodePath = node.get(NodeHeader.PATH);
        if(nodePath.startsWith(oldPath)) {
            final String changed = newPath + nodePath.substring(oldPath.length());
            node.getHeaders().put(NodeHeader.PATH, changed);
        }

        if(node.getHeaders().containsKey(NodeHeader.COPY_FROM_PATH)) {
            final String copyPath = node.get(NodeHeader.COPY_FROM_PATH);
            if(copyPath.startsWith(oldPath)) {
                final String changed = newPath + copyPath.substring(oldPath.length());
                node.getHeaders().put(NodeHeader.COPY_FROM_PATH, changed);
            }
        }

        if(node.getProperties() != null && node.getProperties().containsKey(Property.MERGEINFO)) {
            final String newMergeInfo = updateMergeInfo(node.getProperties().get(Property.MERGEINFO));
            if(newMergeInfo != null) {
                node.getProperties().put(Property.MERGEINFO, newMergeInfo);
            }
        }
        super.consume(node);
    }

    /**
     * @return null if the string was not updated
     */
    private String updateMergeInfo(String currentMergeInfo) {
        MergeInfoData data;
        try {
            data = MergeInfoParser.parse(currentMergeInfo);
        } catch(ParseException ex) {
            throw new RuntimeException(ex);
        }

        boolean changedMergeInfo = false;
        for(MergeInfoData.Path path : data.getPaths()) {
            String pathName = path.getPath();
            if(pathName.startsWith("/" + oldPath)) {
                final String newPathName = "/" + newPath + pathName.substring(oldPath.length() + 1);
                path.setPath(newPathName);
                changedMergeInfo = true;
            }
        }

        if(changedMergeInfo) {
            return data.toString();
        } else {
            return null;
        }
    }
}
