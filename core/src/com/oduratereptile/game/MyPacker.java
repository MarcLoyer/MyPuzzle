package com.oduratereptile.game;

import com.badlogic.gdx.tools.texturepacker.TexturePacker;

/**
 * Created by Marc on 10/3/2017.
 */

// UPDATE: this program is not currently useful! All the ui textures are now built into the skin
//    Also, the skinComposer program works great and is the easiest way to build skins.

// NODO: add code to generate the skin
// Useful links:
//    https://github.com/libgdx/libgdx/wiki/Skin (wiki page)
//    https://github.com/czyzby/gdx-skins (sample skins)
//    http://pimentoso.blogspot.com/2013/04/libgdx-scene2d-skin-quick-tutorial.html (example/tutorial)


public class MyPacker {
    public static void main (String[] args) throws Exception {
        //NODO: delete existing atlas files first (everything is in the skin now. I never run this packer)
        TexturePacker.process("../../AssetDevelopment/MyPuzzle/PackImages", "android/assets", "atlas");
    }
}
