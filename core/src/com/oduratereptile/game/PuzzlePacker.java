package com.oduratereptile.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.ShortArray;

import java.util.ArrayList;

import static com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888;
import static com.badlogic.gdx.graphics.Texture.TextureFilter.Nearest;

/**
 * Created by Marc on 11/8/2017.
 */

public class PuzzlePacker {
    public OrthographicCamera camera;
    public SpriteBatch batch;
    public ShaderProgram shader;

    public Puzzle puzzle;
    public int pageSize;
    public PixmapPacker packer;
    public TextureAtlas atlas=null;

    public ObjectMap<String, PieceData> pieceData = new ObjectMap<String, PieceData>();

    public PuzzlePacker(Puzzle puzzle, int pageSize) {
        this.puzzle = puzzle;
        this.pageSize = pageSize;

        camera = new OrthographicCamera();
        batch = puzzle.gameScreen.game.batch;
        shader = new ShaderProgram(
                Gdx.files.internal("shaders/mesh.vert"),
                Gdx.files.internal("shaders/mesh.frag")
        );
        if (shader.getLog().length()!=0)
            Gdx.app.error("debug", shader.getLog());

        packer = new PixmapPacker(pageSize, pageSize, Pixmap.Format.RGBA8888, 0, false,
                new PixmapPacker.SkylineStrategy());
    }

    public MeshPiece createPiece(int row, int col) {
        return new MeshPiece(row, col);
    }

    public MeshPiece pack(int row, int col) {
        MeshPiece piece = new MeshPiece(row, col);
        return pack(piece);
    }

    public MeshPiece pack(MeshPiece piece) {
        String name = piece.row + "," + piece.col;
        pieceData.put(name, new PieceData(piece.pos, piece.tapSquare));
        packer.pack(name, piece.getPixmap());
        return piece;
    }

    public class PieceData {
        public Vector2 position;
        public BoundingBox tapSquare;

        public PieceData(Vector2 p, BoundingBox t) {
            position = p;
            tapSquare = t;
        }
    }

    public void createAtlas() { getAtlas(); }

    public TextureAtlas getAtlas() {
        if (atlas!=null) return atlas;
        atlas = packer.generateTextureAtlas(Nearest, Nearest, false);
        return atlas;
    }

    public TextureRegion getRegion(int row, int col) {
        String name = row + "," + col;
        return getRegion(name);
    }

    public TextureRegion getRegion(String name) {
        if (atlas==null) return null;
        return atlas.findRegion(name);
    }

    public PieceData getData(int row, int col) {
        String name = row + "," + col;
        return getData(name);
    }

    public PieceData getData(String name) {
        return pieceData.get(name);
    }

    public void save(FileHandle fh) {
        // TODO: implement!
    }

    public void load(FileHandle fh) {
        // TODO: implement!
    }

    public class MeshPiece {
        public int row;
        public int col;
        public Mesh mesh;
        public Vector2 pos;
        public BoundingBox bounds;
        public BoundingBox tapSquare;
        private FrameBuffer fbo;
        private Texture tex=null;
        private Pixmap pix=null;

        public MeshPiece(int row, int col) {
            this.row = row;
            this.col = col;

            // get the shape from the splines
            ArrayList<Vector2> splineShape = getShape(row, col);
            pos = new Vector2(bounds.min.x, bounds.min.y);

            // triangulate it
            FloatArray verts = getVerts(splineShape);
            ShortArray tris = getTris(verts);

            // create the mesh
            mesh = makeMesh(verts, tris, bounds.min);
            bounds.max.sub(bounds.min);
            tapSquare.min.sub(bounds.min);
            tapSquare.max.sub(bounds.min);
            bounds.min.set(0,0,0);
        }

        /** Collect the portions of the splines that pertain to our piece
         *
         * @param row
         * @param col
         * @return
         */
        private ArrayList<Vector2> getShape(int row, int col) {
            ArrayList<Vector2> splineShape = new ArrayList<Vector2>();
            Vector2 upperRight = new Vector2(puzzle.puzzleImg.getWidth(), puzzle.puzzleImg.getHeight());

            int [] corner = new int[3];

            // top
            if (row == (puzzle.numRows-1)) {
                if (col == 0) {
                    splineShape.add(new Vector2(0, upperRight.y));
                } else {
                    splineShape.add(puzzle.colLine[col - 1][puzzle.numRows * 50].cpy());
                }
            } else {
                for (int i=col*50; i<(col+1)*50; i++) { splineShape.add(puzzle.rowLine[row][i].cpy()); }
            }
            corner[0] = splineShape.size();

            // right
            if (col == (puzzle.numCols-1)) {
                if (row == (puzzle.numRows-1)) {
                    splineShape.add(new Vector2(upperRight.x, upperRight.y));
                } else {
                    splineShape.add(puzzle.rowLine[row][puzzle.numCols * 50].cpy());
                }
            } else {
                for (int i=(row+1)*50; i>row*50; i--) { splineShape.add(puzzle.colLine[col][i].cpy()); }
            }
            corner[1] = splineShape.size();

            // bottom
            if (row == 0) {
                if (col == (puzzle.numCols-1)) {
                    splineShape.add(new Vector2(upperRight.x, 0));
                } else {
                    splineShape.add(puzzle.colLine[col][0].cpy());
                }
            } else {
                for (int i=(col+1)*50; i>col*50; i--) { splineShape.add(puzzle.rowLine[row-1][i].cpy()); }
            }
            corner[2] = splineShape.size();

            // left
            if (col == 0) {
                if (row == 0) {
                    splineShape.add(new Vector2(0, 0));
                } else {
                    splineShape.add(puzzle.rowLine[row - 1][0].cpy());
                }
            } else {
                for (int i=row*50; i<(row+1)*50; i++) { splineShape.add(puzzle.colLine[col-1][i].cpy()); }
            }

            splineShape.add(splineShape.get(0));
            computeTapSquare(splineShape, corner[0], corner[1], corner[2]);
            splineShape.remove(splineShape.size()-1);

            return splineShape;
        }

        private void computeTapSquare(ArrayList<Vector2> s, int ur, int lr, int ll) {
            Vector2 top = getTapSquareSide(0, s, 0, ur); // x==max, y==min
            Vector2 right = getTapSquareSide(1, s, ur, lr);
            Vector2 bottom = getTapSquareSide(2, s, lr, ll);
            Vector2 left = getTapSquareSide(3, s, ll, s.size()-1);

            bounds = new BoundingBox(
                    new Vector3(
                            (float)Math.floor(left.y),
                            (float)Math.floor(bottom.y),
                            0),
                    new Vector3(
                            (float)Math.ceil(right.x),
                            (float)Math.ceil(top.x),
                            0)
            );

            float ratio = 0.4f;
            tapSquare = new BoundingBox(
                    new Vector3(
                            interp(ratio, left.x, left.y),
                            interp(ratio, bottom.x, bottom.y),
                            0),
                    new Vector3(
                            interp(ratio, right.y, right.x),
                            interp(ratio, top.y, top.x),
                            0)
            );
        }

        private float interp(float ratio, float a, float b) {
            return a*(1-ratio) + b*ratio;
        }

        private Vector2 getTapSquareSide(int side, ArrayList<Vector2> s, int start, int stop) {
            float ratio = 0.4f;
            boolean useY = (side==0)||(side==2); // 0==top, 1=right, 2=bottom, 3==left
            float max = (useY)? s.get(stop).y: s.get(stop).x;
            float min = (useY)? s.get(stop).y: s.get(stop).x;
            for (int i=start; i<stop; i++) {
                float a = (useY)? s.get(i).y: s.get(i).x;
                if (a>max) max=a;
                if (a<min) min=a;
            }
            return new Vector2(max, min);
        }


        /**
         * Converts a list of Vector2 to a list of floats
         * @param splineShape
         * @return
         */
        private FloatArray getVerts(ArrayList<Vector2> splineShape) {
            FloatArray verts = new FloatArray(2*splineShape.size());
            for (Vector2 v: splineShape) {
                verts.addAll(v.x, v.y);
            }

            return verts;
        }

        /**
         * Generates triangles for the given list of vertices
         * @param verts
         * @return
         */
        private ShortArray getTris(FloatArray verts) {
            EarClippingTriangulator triangulator = new EarClippingTriangulator();
            return triangulator.computeTriangles(verts);
        }

        /**
         * Builds a mesh object from the vertices and indices (triangles), then translates
         * it to the origin.
         * @param verts
         * @param tris
         * @param min
         * @return
         */
        private Mesh makeMesh(FloatArray verts, ShortArray tris, Vector3 min) {
            Mesh mesh;
            int numVerts = verts.size/2;
            int numInds = tris.size;
            int vSize = 5; // pos(x,y,z), texture(u,v)

            float [] vertices = new float[vSize*numVerts];
            for (int i=0; i<numVerts; i++) {
                vertices[i*vSize]   = verts.get(i*2)   - min.x;
                vertices[i*vSize+1] = verts.get(i*2+1) - min.y;
                vertices[i*vSize+2] = 0f;
                vertices[i*vSize+3] = verts.get(i*2) / puzzle.puzzleImg.getWidth();
                vertices[i*vSize+4] = 1f - (verts.get(i*2+1) / puzzle.puzzleImg.getHeight());
            }
            short [] indices = new short[numInds];
            for (int i=0; i<numInds; i++) {
                indices[i] = tris.get(i);
            }

            mesh = new Mesh(true, numVerts, numInds,
                    new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
                    new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE+"0")
            );
            mesh.setVertices(vertices);
            mesh.setIndices(indices);

            // move the mesh by min  <-- I think this was already done when we built the vertices array
//            Matrix4 mat = new Matrix4(new Vector3(min.x,min.y,0), new Quaternion(), new Vector3(1,1,1));
//            mesh.transform(mat);

            return mesh;
        }

        public Texture getTexture() {
            if (tex!=null) return tex;

            int bufferWidth = getWidth();
            int bufferHeight = getHeight();

            fbo = new FrameBuffer(RGBA8888, bufferWidth, bufferHeight, false);
            TextureRegion fboTex = new TextureRegion(fbo.getColorBufferTexture(), 0, 0, bufferWidth, bufferHeight);
            fbo.getColorBufferTexture().setFilter(Nearest, Nearest);
            fboTex.flip(false, true);

            camera.setToOrtho(false, bufferWidth, bufferHeight);
            batch.setProjectionMatrix(camera.combined);

            fbo.begin();
            Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
            Gdx.gl20.glEnable(GL20.GL_TEXTURE_2D);
            Gdx.gl20.glDisable(GL20.GL_BLEND);
            Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            puzzle.puzzleImgTex.bind();
            shader.begin();
            shader.setUniformMatrix("u_projTrans", batch.getProjectionMatrix());
            shader.setUniformi("u_texture", 0);
            mesh.render(shader, GL20.GL_TRIANGLES);
            shader.end();

            fbo.end();

            tex = fboTex.getTexture();
            return tex;
        }

        public Pixmap getPixmap() {
            if (pix!=null) return pix;
            if (tex==null) tex = getTexture();

//            Gdx.gl20.glBindFramebuffer(GL20.GL_READ_FRAMEBUFFER, fbo.getFramebufferHandle());
//            Gdx.gl20.glReadBuffer(GL20.GL_COLOR_ATTACHMENT0);
            fbo.begin();
            pix = ScreenUtils.getFrameBufferPixmap(0, 0, fbo.getWidth(), fbo.getHeight());
            fbo.end();
//            Gdx.gl20.glBindFramebuffer(GL20.GL_READ_FRAMEBUFFER, 0);
//            Gdx.gl20.glReadBuffer(GL20.GL_BACK);
            return pix;


            // This code had a runtime error: This TextureData implementation does not return a Pixmap
//            if (!tex.getTextureData().isPrepared()) tex.getTextureData().prepare();
//            return tex.getTextureData().consumePixmap();
        }

        public int getWidth() { return (int)(bounds.max.x - bounds.min.x); }
        public int getHeight() { return (int)(bounds.max.y - bounds.min.y); }
    }
}
