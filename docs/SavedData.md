Let's be honest, [this page](https://docs.neoforged.net/docs/datastorage/saveddata/) is not the greatest.

So, I asked the AI about it.

---

In NeoForge, `SavedData` is a mechanism for storing persistent world-specific data, separate from the Level itself. It's ideal for mod data that needs to persist across game sessions and world loads.

To implement `SavedData`, you'll typically:

* Create a Class Extending `SavedData`: This class will hold your data and handle its loading and saving.
* Implement load and save Methods: These methods will handle reading data from and writing data to a `CompoundTag`, which is a key-value structure used for NBT data.
* Provide a `SavedData.Factory`: This factory will be used to create instances of your `SavedData` class when needed.
* Register the `SavedData` with a `DimensionDataStorage`: This will make your `SavedData` loadable and savable within a specific level. The Overworld is commonly used for level-agnostic data. 

NeoForged docs provides an example SavedData implementation. This includes a class extending `SavedData` with methods for creating, loading, and saving data using a `CompoundTag`. It also shows how to attach the `SavedData` to a level using `DimensionDataStorage#computeIfAbsent`.

```java
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class ExampleSavedData extends SavedData {

    private int myIntValue;
    private String myStringValue;

    public static ExampleSavedData create() {
        return new ExampleSavedData();
    }

    public static ExampleSavedData load(CompoundTag tag) {
        ExampleSavedData data = ExampleSavedData.create();
        data.myIntValue = tag.getInt("myIntValue");
        data.myStringValue = tag.getString("myStringValue");
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putInt("myIntValue", myIntValue);
        tag.putString("myStringValue", myStringValue);
        return tag;
    }

    public int getMyIntValue() {
        return myIntValue;
    }

    public void setMyIntValue(int value) {
        this.myIntValue = value;
        setDirty();
    }

    public String getMyStringValue() {
        return myStringValue;
    }

    public void setMyStringValue(String value) {
        this.myStringValue = value;
        setDirty();
    }
}
```

To use this with a level, you can retrieve or create an instance via `DimensionDataStorage#computeIfAbsent`:

```java
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.DimensionDataStorage;

MinecraftServer server = ...;

DimensionDataStorage overworldDataStorage = server.overworld().getDataStorage();

ExampleSavedData mySavedData = overworldDataStorage.computeIfAbsent(
new SavedData.Factory<>(
ExampleSavedData::create,
ExampleSavedData::load,
null
),
"my_saved_data_name"
);

mySavedData.setMyIntValue(10);
mySavedData.setMyStringValue("Hello, World!");
```
Calling `setDirty()` is necessary to ensure data is saved when changed. This method allows persistent data storage at the level level, independent of specific blocks. 