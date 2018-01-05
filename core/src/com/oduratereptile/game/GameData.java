package com.oduratereptile.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Created by Marc on 1/5/2018.
 */
public class GameData {
    public String puzzleName = "undefined";
    public int rows = 0;
    public int cols = 0;
    // TODO: save the Texture instead of just these two fields?
    public int puzzleImageWidth;
    public int puzzleImageHeight;
    public ObjectMap<String, PuzzlePiece> puzzlePieces;
    public ObjectMap<String, PuzzleGroup> puzzleGroups;
    public int groupCount = 0;
    public String textureAtlasFilename = null;

    public GameData() {
        puzzlePieces = new ObjectMap<String, PuzzlePiece>();
        puzzleGroups = new ObjectMap<String, PuzzleGroup>();
    }

    public GameData(ObjectMap<String, PuzzlePiece> p, ObjectMap<String, PuzzleGroup> g) {
        puzzlePieces = p;
        puzzleGroups = g;
    }

    public String getBasename() {
        return puzzleName + "_" + rows + "_" + cols;
    }

    /**
     * this routine assumes all pieces are in their solved state.
     */
    public void setPieceNeighbors() {
        for (int i=0; i<rows; i++) {
            for (int j=0; j<cols; j++) {
                puzzlePieces.get(i+","+j).setNeighbors(
                        (i==rows-1)? null: puzzlePieces.get((i+1)+","+j),
                        (j==cols-1)? null: puzzlePieces.get(i+","+(j+1)),
                        (i==0)? null: puzzlePieces.get((i-1)+","+j),
                        (j==0)? null: puzzlePieces.get(i+","+(j-1))
                );
            }
        }
    }

    public static GameData restoreGameData(String basename) {
        Json json = new Json();

        FileHandle fh = Gdx.files.local(basename + "/savedGame.json");
        String s = Base64Coder.decodeString(fh.readString());
        GameData gameData = json.fromJson(GameData.class, s);

        // TODO: fix linkages

        return gameData;
    }
}
