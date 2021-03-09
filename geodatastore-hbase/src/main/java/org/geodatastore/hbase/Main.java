package org.geodatastore.hbase;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureWriter;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.Hints;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.filter.text.ecql.ECQL;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.geomesa.utils.geotools.SimpleFeatureTypes;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.sort.SortBy;
import org.opengis.filter.sort.SortOrder;
import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.*;
public class Main {
    public static final String ZK = "192.168.1.5:2181"; //Zookeeper地址
    public static void main(String args[]){
        try{
            DataStore ds=null;
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");
            //配置连接参数
            Map<String, String> params= new HashMap<>();
            params.put("hbase.zookeepers",ZK);
            params.put("hbase.catalog","test_points_2");
            //初始化DataStore
            ds= DataStoreFinder.getDataStore(params);
            //创建SimpleFeatureType定义表结构
            String sft_name="test_points_2";
            SimpleFeatureType sft=
                    SimpleFeatureTypes.createType(sft_name, "name:String,dtg:Date,*geom:Point:srid=4326");
            //指定压缩方式gz
            sft.getUserData().put("geomesa.table.compression.enabled", "true");
            sft.getUserData().put("geomesa.table.compression.type", "gz");
            //创建数据表
            ds.createSchema(sft);
            /*
             * GeometryFactory 用来创建空间对象
             */
            GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
            SimpleFeatureBuilder builder = new SimpleFeatureBuilder(sft);
            //构造空间数据（点）
            Point point1 = geometryFactory.createPoint(new Coordinate(120.301,35.086));
            Point point2 = geometryFactory.createPoint(new Coordinate(120.301,35.076));
            Point point3 = geometryFactory.createPoint(new Coordinate(120.301,35.066));
            //构造点SimpleFeature
            List<SimpleFeature> features=new ArrayList<>();
            features.add(builder.buildFeature("1", new Object[]{"point1",new Date(),point1}));
            features.add(builder.buildFeature("2", new Object[]{"point2",new Date(),point2}));
            features.add(builder.buildFeature("3", new Object[]{"point3",new Date(),point3}));
            //要素入库
            SimpleFeatureWriter writer=(SimpleFeatureWriter)ds.getFeatureWriterAppend(sft_name, Transaction.AUTO_COMMIT);
            for(SimpleFeature feature:features){
                SimpleFeature toWrite=writer.next();
                toWrite.setAttributes(feature.getAttributes());
                toWrite.getUserData().putAll(feature.getUserData());
                writer.write();
            }
            writer.close();
            //构造时空查询条件
            long t1=format.parse("2019-01-19 11:45:00").getTime();
            long t2=format.parse("2019-02-21 12:15:00").getTime();
            String sortField="dtg";//排序字段，这里设为时间
            FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
            SortBy[] sort = new SortBy[]{ff.sort(sortField, SortOrder.DESCENDING)};
            //构造Query对象用于查询
            Query query = new Query(sft_name, ECQL.toFilter( "bbox(geom,120,20,130,40) AND dtg >= "+t1+" AND dtg <= "+t2));
            query.setSortBy(sort);
            SimpleFeatureCollection result=ds.getFeatureSource(sft_name).getFeatures(query);
            SimpleFeatureIterator iterator=result.features();
            //输出查询结果
            long sum = 0;
            while (iterator.hasNext()) {
                System.out.println(iterator.next());
                sum++;
            }
            System.out.println("查询总数：" + sum);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}