/**
 *
 */
package com.redhat.gpte;

import java.util.Arrays;

/**
 * @author prakrish
 *
 */
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;

public class App {

    public static void main(String[] args) {

        //Input the Data  
        List<Applicant> inputData = Arrays.asList(
                new Applicant(1, "Jerry", "Seinfeld", 10000, 568),
                new Applicant(2, "Michael", "Richards", 12000, 654),
                new Applicant(3, "Cosmo", "Kramer", 100, 568),
                new Applicant(4, "Elaine", "Benes", 1000000, 788),
                new Applicant(5, "Susan", "Ross", 10, 788),
                new Applicant(6, "Jake", "Harper", 34000, 900),
                new Applicant(7, "John", "Cryer", 12000, 457),
                new Applicant(8, "Walden", "Schimdt", 10000, 300),
                new Applicant(9, "Julie", "Dreyfus", 20000, 721),
                new Applicant(10, "Jason", "Alexandar", 25000, 590)
        );

        SparkConf conf = new SparkConf();
        //as recomended by databricks
        conf.set("spark.hadoop.fs.s3a.multipart.threshold", "2097152000");
        conf.set("spark.hadoop.fs.s3a.multipart.size", "104857600");
        conf.set("spark.hadoop.fs.s3a.connection.maximum", "500");
        conf.set("spark.hadoop.fs.s3a.connection.timeout", "600000");
        //Spark session is the main entry point for interfacing with it
        //remember that spark session is 'safe' and can be shared among threads

        SparkSession spark = SparkSession.builder()
                .appName("Simple Application")
                .config(conf)
                .master("local[*]")
                .getOrCreate();

        JavaSparkContext jsc = JavaSparkContext.fromSparkContext(spark.sparkContext());
        KieBase rules = loadRules();
        Broadcast<KieBase> broadcastRules = jsc.broadcast(rules);

        //Spark used to parallelize the input data and fire the rules for the rule which is in com/redhat/approval.drl
        Dataset<Applicant> applicantsDS = spark.createDataset(inputData, Encoders.bean(Applicant.class));
        applicantsDS.show(false);


        applicantsDS
                .map(a -> applyRules(broadcastRules.value(), a), Encoders.bean(Applicant.class))
                .filter(a -> a.isApproved());
        
        applicantsDS.show(false);
        //       .count();  //First Action Performed on Spark

//        System.out.println("Number of applicants approved: " + numApproved);
        //applicants.saveAsTextFile("com.redhat.gpte"); // 2nd Action is Performed on this Job to Save the File.
        spark.close();
    }

//Load the Rules (approval.drl) in the container 
    public static KieBase loadRules() {
        KieServices kieServices = KieServices.Factory.get();
        KieContainer kieContainer = kieServices.getKieClasspathContainer();
        return kieContainer.getKieBase();
    }

    // Fire/apply the rules if the credit score > 600
    public static Applicant applyRules(KieBase base, Applicant a) {
        StatelessKieSession session = base.newStatelessKieSession();
        session.execute(a);
        return a;
    }
}
