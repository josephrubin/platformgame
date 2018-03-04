package box.shoe.gameutils.map;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;

public class MapLoader
{
    private static java.util.Map<String, Tileset> cachedTilesets = new java.util.HashMap<>();

    public static Map fromXml(AssetManager assetManager, String xmlFilePath) throws ParserConfigurationException, IOException, SAXException
    {
        // We will use a DOM parser. Create the factory.
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setIgnoringElementContentWhitespace(true);

        // todo: description of the flags we set here
        // Use the factory to create the builder to parse our file, then parse.
        DocumentBuilder xmlDocumentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document xmlDocument = xmlDocumentBuilder.parse(assetManager.open(xmlFilePath));

        // Get the root 'map' element.
        Element root = xmlDocument.getDocumentElement();

        // Get all of the 'tileset' elements.
        NodeList tilesetNodeList = root.getElementsByTagName("tileset");
        int tilesetNodeListLength = tilesetNodeList.getLength();

        Tileset[] tilesets = new Tileset[tilesetNodeListLength];
        for (int i = 0; i < tilesetNodeListLength; i++)
        {
            // Get the 'tileset' element. We know they're Element Nodes (getElementsByTagName), so this cast is valid.
            Element tilesetElement = (Element) tilesetNodeList.item(i);
            // There is only one image for each tileset. We can't use firstChild because there are text/attribute nodes.
            Element tilesetImageElement = (Element) tilesetElement.getElementsByTagName("image").item(0);
            Tileset tileset;

            // Save the source of the tileset's image now, since we will only get the
            // other attributes if we do not find this tileset in the cache.
            String tilesetImageElementSource = tilesetImageElement.getAttribute("source");

            if (cachedTilesets.containsKey(tilesetImageElementSource))
            {
                // We found the tileset in the cache, so simply retrieve it.
                tileset = cachedTilesets.get(tilesetImageElementSource);
            }
            else
            {
                // The tileset was not in the cache, so we must create a new one.
                // First, load up the image from its source, and create a Bitmap...
                Bitmap image = BitmapFactory.decodeStream(assetManager.open(tilesetImageElementSource.substring(3))); //fixme: somehow fix uri's which are relative to tmx file, they should be relative to assets folder.

                // ...then create the new tileset...
                tileset = new Tileset.Builder()
                        .setName(tilesetElement.getAttribute("name"))
                        .setFirstGid(Integer.parseInt(tilesetElement.getAttribute("firstgid")))
                        .setTileWidth(Integer.parseInt(tilesetElement.getAttribute("tilewidth")))
                        .setTileHeight(Integer.parseInt(tilesetElement.getAttribute("tileheight")))
                        .setImagePath(tilesetImageElementSource)
                        .setImageWidth(Integer.parseInt(tilesetImageElement.getAttribute("width")))
                        .setImageHeight(Integer.parseInt(tilesetImageElement.getAttribute("height")))
                        .setImage(image)
                        .build();

                // ...and add it to the cache.
                cachedTilesets.put(tilesetImageElementSource, tileset);
            }

            tilesets[i] = tileset;
        }

        Map map = new Map.Builder()
                .setColumns(Integer.parseInt(root.getAttribute("width")))
                .setRows(Integer.parseInt(root.getAttribute("height")))
                .setTileWidth(Integer.parseInt(root.getAttribute("tilewidth")))
                .setTileHeight(Integer.parseInt(root.getAttribute("tileheight")))
                .build();

        // Now we will process each layer one by one.
        NodeList layerNodeList = root.getElementsByTagName("layer"); //todo: if invisible layer, do not generate bitmap
        int layerNodeListLength = layerNodeList.getLength();
        for (int i = 0; i < layerNodeListLength; i++)
        {
            Element layerElement = (Element) layerNodeList.item(i);
            byte[][] layerRelativeTileGids = new byte[map.COLUMNS][map.ROWS];
            Bitmap layerBitmap = Bitmap.createBitmap(map.COLUMNS * map.TILE_WIDTH, map.ROWS * map.TILE_HEIGHT, Bitmap.Config.ARGB_8888);
            Canvas layerBitmapCanvas = new Canvas(layerBitmap);
            // And for each layer, process the tiles one by one.
            NodeList tileNodeList = layerElement.getElementsByTagName("tile");
            int tileNodeListLength = tileNodeList.getLength();
            for (int j = 0; j < tileNodeListLength; j++)
            {
                Element tileElement = (Element) tileNodeList.item(j);
                int absoluteTileGid = Integer.parseInt(tileElement.getAttribute("gid"));
                int relativeTileGid = -1;
                // We want to get the relative GID for our tile, so search the tilesets backwards.
                Tileset tileTileset = null;
                for (int k = tilesets.length - 1; k >= 0; k--)
                {
                    Tileset tileset = tilesets[k];
                    if (tileset.FIRST_GID <= absoluteTileGid)
                    {
                        tileTileset = tileset;
                        relativeTileGid = absoluteTileGid - tileset.FIRST_GID; //todo: we should add one to be consistent with GIDs starting at 11, but i think that the current way (start at 0) is better
                        if (relativeTileGid > Byte.MAX_VALUE)
                        {
                            throw new IllegalArgumentException("Tileset (" + tileset.NAME + ") has more than " + Byte.MAX_VALUE + " tiles!");
                        }
                        break;
                    }
                }
                if (tileTileset == null)
                {
                    throw new IllegalArgumentException("Malformed XML! Found a tile (" + tileElement + "+ whose GID" +
                            " (" + absoluteTileGid + ") is not contained in any tileset!");
                }
                int tileColumn = j % map.COLUMNS;
                int tileRow = j / map.COLUMNS;
                layerRelativeTileGids[tileColumn][tileRow] = (byte) relativeTileGid;
                //todo: it may be useful to the user to have the absolute gid instead of the relative one.
                //todo: or store some bit pattern that encodes the tileset used, or have both relative and absolute.

                int destX = tileColumn * map.TILE_WIDTH;
                int destY = tileRow * map.TILE_HEIGHT;

                int sourceX = (relativeTileGid % (tileTileset.IMAGE_WIDTH / tileTileset.TILE_WIDTH)) * map.TILE_WIDTH;
                int sourceY = (relativeTileGid / (tileTileset.IMAGE_WIDTH / tileTileset.TILE_WIDTH)) * map.TILE_HEIGHT;

                // Assemble the layer bitmap with each tile.
                //todo: check for some attribute or option that says if this layer should generate a bitmap or not.
                //todo: do not create two rectangles each time, use old rectangle.
                layerBitmapCanvas.drawBitmap(tileTileset.IMAGE, new Rect(sourceX, sourceY, sourceX + map.TILE_WIDTH, sourceY + map.TILE_HEIGHT), new Rect(destX, destY, destX + map.TILE_WIDTH, destY + map.TILE_HEIGHT), null);
            }
        }

        return map;
    }

    private static class Tileset
    {//todo: needs a cleanup method to recycle the bitmap
        public final String NAME;

        public final int FIRST_GID;

        public final int TILE_WIDTH;
        public final int TILE_HEIGHT;

        public final String IMAGE_PATH;
        public final int IMAGE_WIDTH;
        public final int IMAGE_HEIGHT;
        public final Bitmap IMAGE;

        private Tileset(String name, int firstGid, int tileWidth, int tileHeight, String imagePath, int imageWidth, int imageHeight, Bitmap image)
        {
            NAME = name;
            FIRST_GID = firstGid;
            TILE_WIDTH = tileWidth;
            TILE_HEIGHT = tileHeight;
            IMAGE_PATH = imagePath;
            IMAGE_WIDTH = imageWidth;
            IMAGE_HEIGHT = imageHeight;
            IMAGE = image;
        }

        @Override
        public String toString() {
            return "Tileset{" +
                    "NAME='" + NAME + '\'' +
                    ", FIRST_GID=" + FIRST_GID +
                    ", TILE_WIDTH=" + TILE_WIDTH +
                    ", TILE_HEIGHT=" + TILE_HEIGHT +
                    ", IMAGE_PATH='" + IMAGE_PATH + '\'' +
                    ", IMAGE_WIDTH=" + IMAGE_WIDTH +
                    ", IMAGE_HEIGHT=" + IMAGE_HEIGHT +
                    '}';
        }

        public static class Builder
        {
            private String name;
            private int firstGid;
            private int tileWidth;
            private int tileHeight;
            private String imagePath;
            private int imageWidth;
            private int imageHeight;
            private Bitmap image;

            public Builder setName(String name) {
                this.name = name;
                return this;
            }

            public Builder setFirstGid(int firstGid) {
                this.firstGid = firstGid;
                return this;
            }

            public Builder setTileWidth(int tileWidth) {
                this.tileWidth = tileWidth;
                return this;
            }

            public Builder setTileHeight(int tileHeight) {
                this.tileHeight = tileHeight;
                return this;
            }

            public Builder setImagePath(String imagePath) {
                this.imagePath = imagePath;
                return this;
            }

            public Builder setImageWidth(int imageWidth) {
                this.imageWidth = imageWidth;
                return this;
            }

            public Builder setImageHeight(int imageHeight) {
                this.imageHeight = imageHeight;
                return this;
            }

            public Builder setImage(Bitmap image) {
                this.image = image;
                return this;
            }

            public Tileset build()
            {
                return new Tileset(name, firstGid, tileWidth, tileHeight, imagePath, imageWidth, imageHeight, image);
            }
        }
    }
}
