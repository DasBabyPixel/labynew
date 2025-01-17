package gamelauncher.gles.modelloader;

import gamelauncher.engine.resource.ResourceStream;
import gamelauncher.engine.util.GameException;

public interface ModelSubLoader {

    byte[] convertModel(ResourceStream in) throws GameException;

}
