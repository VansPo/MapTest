MapTest
=======

TileMap implementation

## Usage

Create new instance of TileView:
```
TileView tileView = new TileView();
```
Then create new instance of TileProvider, and define a BitmapDecoder for it. Register your Provider in TileView:
```
demoProvider = new DefaultTileProvider(this);
demoProvider.setBitmapDecoder(new HttpBitmapDecoder(33198, 22539, "http://b.tile.opencyclemap.org/cycle/16/%row%/%col%.png"));
tileView.registerProvider(demoProvider);
```

That's it!
