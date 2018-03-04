package box.shoe.gameutils.map;

public class Map
{
    //todo: change name, get rid of MAP_, and call COLUMNS, ROWS, THESE VALUES ARE IN TILES
    public final int COLUMNS;
    public final int ROWS;
    //TODO: THESE VALS ARE IN PIXELS, THIS IS VERY CONFUSING!
    public final int TILE_WIDTH;
    public final int TILE_HEIGHT;

    public Map(int columns, int rows, int tileWidth, int tileHeight)
    {
        COLUMNS = columns;
        ROWS = rows;

        TILE_WIDTH = tileWidth;
        TILE_HEIGHT = tileHeight;
    }

    public static class Builder
    {
        private int columns;
        private int rows;

        private int tileWidth;
        private int tileHeight;

        public Builder()
        {

        }

        public Map build()
        {
            return new Map(columns, rows, tileWidth, tileHeight);
        }

        public Builder setColumns(int columns)
        {
            this.columns = columns;
            return this;
        }

        public Builder setRows(int rows)
        {
            this.rows = rows;
            return this;
        }

        public Builder setTileWidth(int tileWidth)
        {
            this.tileWidth = tileWidth;
            return this;
        }

        public Builder setTileHeight(int tileHeight)
        {
            this.tileHeight = tileHeight;
            return this;
        }
    }
}
