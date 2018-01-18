package com.oduratereptile.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
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
    public int puzzleImageWidth;
    public int puzzleImageHeight;
    public ObjectMap<String, PuzzlePiece> puzzlePieces;
    public ObjectMap<String, PuzzleGroup> puzzleGroups;
    public int groupCount = 0;
    public String textureAtlasFilename = null;

    public transient TextureAtlas atlas;
    public transient Pixmap thumbnail;

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

    public final float THUMBNAIL_WIDTH = 300.0f;

    public void createThumbnail(Pixmap original) {
        float scale = THUMBNAIL_WIDTH / (float)original.getWidth();
        thumbnail = new Pixmap(
                (int)(original.getWidth() * scale),
                (int)(original.getHeight() * scale),
                original.getFormat());
        thumbnail.setFilter(Pixmap.Filter.BiLinear);
        thumbnail.drawPixmap(original,
                0, 0,
                original.getWidth(), original.getHeight(),
                0, 0,
                thumbnail.getWidth(), thumbnail.getHeight());
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

    public TextureAtlas getAtlas() {
        if (atlas != null) return atlas;
        if (textureAtlasFilename == null) return null;
        atlas = new TextureAtlas(Gdx.files.local(textureAtlasFilename));
        return atlas;
    }

    public void saveGameData() {
        Json json = new Json();

        // write the data to a file...
        String s = json.toJson(this);
        s = Base64Coder.encodeString(s);
        FileHandle fh = Gdx.files.local(getBasename() + "/savedGame.json");
//        fh.writeString(json.prettyPrint(s), false);
        fh.writeString(s, false);

        // write the thumbnail image
        fh = Gdx.files.local(getBasename() + "/thumbnail.png");
        PixmapIO.writePNG(fh, thumbnail);

        // note: PuzzleMaker is responsible for writing the atlas to a file
    }

    public static GameData restoreGameData(String basename) {
        GameData gameData = restoreGameHeader(basename);

        TextureAtlas atlas = gameData.getAtlas();

        // fix linkages and textureRegions
        for (PuzzlePiece p: gameData.puzzlePieces.values()) {
            p.restoreTextureRegion(atlas);
            p.restoreNeighborLinkages(gameData.puzzlePieces);
            p.restoreGroupLinkage(gameData.puzzleGroups);
        }

        for (PuzzleGroup g: gameData.puzzleGroups.values()) {
            g.restorePieceLinkages(gameData.puzzlePieces);
        }

        return gameData;
    }

    public static GameData restoreGameHeader(String basename) {
        Json json = new Json();

        FileHandle fh = Gdx.files.local(basename + "/savedGame.json");
        String s = Base64Coder.decodeString(fh.readString());
        GameData gameData = json.fromJson(GameData.class, s);

        fh = Gdx.files.local(basename + "/thumbnail.png");
        if (fh.exists()) {
            gameData.thumbnail = new Pixmap(fh);
        }

        return gameData;
    }

    public void dispose() {
        if (thumbnail != null) thumbnail.dispose();
        if (atlas != null) atlas.dispose();
    }
}
