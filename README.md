JSONAnnotatedParser
===================

Add semantical JSON parsing to your models using java annotations.

### Overview

With JSONAnnotatedParser you can go from

```java
public static Album albumFromJSON(JSONObject albumJSON)
{
    Long id = null;
    String artistName = null;
    String name = null;
            
    try
    {
        id =  albumJSON.getLong("id");
        artistName =  albumJSON.getString("artist");
        name =  albumJSON.getString("name");
    }
    catch(Exception e){...}
            
    tracks = new LinkedList<Track>();
            
    try
    {
        JSONArray tracksJSONArray = albumJSON.getJSONArray("tracks");
              
        for(int i=0; i < tracksJSONArray.length(); i++)
            tracks.add(trackFromJSON(tracksJSONArray.getJSONObject(i)));
    }
    catch(Exception e){...}
            
    return new Album(id, artistName, name, tracks);
}
    ...
```
  
To

```java
public class Album implements Parsable
{ 
    @JSON.Value(key = "id", type = JSON.Type.LONG)
    private Long id;
    
    @JSON.Value(key = "name", type = JSON.Type.STRING)
    private String name;
    
    @JSON.Value(key = "artist", type = JSON.Type.STRING)
    private String artistName;
    
    @JSON.ValueCollection(key = "tracks", of = JSON.Type.OBJ)
    @JSON.ParseAs(Track.class)
    private List<Track> tracks;
    
    ...
    
    public static Album fromJSON(JSONObject albumJSON)
    {
        return FromJSON.create(albumJSON, Album.class);
    }
}
```
