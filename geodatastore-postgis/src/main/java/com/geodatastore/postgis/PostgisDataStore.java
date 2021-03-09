package com.geodatastore.postgis;

import org.geotools.data.*;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.postgis.PostgisNGDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeImpl;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.identity.FeatureId;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class PostgisDataStore {

    private DataStore dataStore = null;
    private GeometryFactory geometryFactory = null;
    private final String tableName = "pois";
    //获取类型
    private SimpleFeatureType type = null;
    public PostgisDataStore() {
        this.geometryFactory = new GeometryFactory();
    };

    public  DataStore getDataStore(Map<String,Object> map) throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put(PostgisNGDataStoreFactory.DBTYPE.key, "postgis");
        params.put(PostgisNGDataStoreFactory.HOST.key, map.get("host"));
        params.put(PostgisNGDataStoreFactory.PORT.key, 5432);
        params.put(PostgisNGDataStoreFactory.SCHEMA.key, map.get("schema"));
        params.put(PostgisNGDataStoreFactory.DATABASE.key, map.get("database"));
        params.put(PostgisNGDataStoreFactory.USER.key, map.get("user"));
        params.put(PostgisNGDataStoreFactory.PASSWD.key, map.get("passwd"));

        dataStore = DataStoreFinder.getDataStore(params);

        if (dataStore == null) {
            throw new IOException("数据库连接未成功");
        } else {
            System.out.println("数据库连接成功");
        }
        return dataStore;
    }

    public SimpleFeature createSimplePointFeatureByLonLat(Double lon,Double lat) throws IOException {
        SimpleFeatureSource simpleFeatureSource = null;

        Point point = null;
        //构建要素
        SimpleFeature feature = null;
        if (dataStore != null) {
            simpleFeatureSource = dataStore.getFeatureSource(tableName);
            type = simpleFeatureSource.getSchema();
            SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(type);
            point = geometryFactory.createPoint(new Coordinate(lon,lat));
            List<Object> resultList = new ArrayList<>();
            resultList.add(point);
            resultList.add(1314);
            resultList.add("abcdfs");
            featureBuilder.addAll(resultList);

            feature = featureBuilder.buildFeature("poi1");

        } else {
            throw new IOException("数据库连接未成功");
        }
        return feature;
    };

    public SimpleFeatureCollection getFeaturesCollection(SimpleFeatureType type,List<SimpleFeature> features) throws IOException{
        SimpleFeatureCollection collection = new ListFeatureCollection(type,features);
        return  collection;
    }

    public void  insertData(List<SimpleFeature> features) throws IOException {

    }

    public static void main(String[] args) throws IOException {
        PostgisDataStore postgisDataStore = new PostgisDataStore();
        Map<String, Object> params = new HashMap<>();
        params.put(PostgisNGDataStoreFactory.HOST.key, "localhost");
        params.put(PostgisNGDataStoreFactory.SCHEMA.key, "public");
        params.put(PostgisNGDataStoreFactory.DATABASE.key, "postgres");
        params.put(PostgisNGDataStoreFactory.USER.key, "postgres");
        params.put(PostgisNGDataStoreFactory.PASSWD.key,  "postgres");
        DataStore dataStore = postgisDataStore.getDataStore(params);

        SimpleFeature feature = postgisDataStore.createSimplePointFeatureByLonLat(112.1,38.1);
        List<SimpleFeature> features = new ArrayList<>();
        features.add(feature);
        SimpleFeatureSource featureSource = dataStore.getFeatureSource(postgisDataStore.tableName);

        if( featureSource instanceof SimpleFeatureStore){
            SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
            SimpleFeatureCollection featureCollection= postgisDataStore.getFeaturesCollection(postgisDataStore.type,features);
            //创建事务
            Transaction session = new DefaultTransaction("Adding");
            featureStore.setTransaction( session );
            try {
                List<FeatureId> added = featureStore.addFeatures( featureCollection );
                System.out.println( "Added "+added );
                //提交事务
                session.commit();
            }
            catch (Throwable t){
                System.out.println( "Failed to add features: "+t );
                try {
                    //事务回归
                    session.rollback();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}
