/**
 *
 */
package com.redhat.gpte;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author prakrish
 *
 */
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.kie.api.definition.KiePackage;
import org.kie.api.runtime.KieSession;

public class App {

    public static void main(String[] args) {

        //Input the Data  
        List<Applicant> inputData = Arrays.asList(
                new Applicant(1, "Jerry", "Seinfeld", 10000, 568, "a"),
                new Applicant(2, "Michael", "Richards", 12000, 654, "a"),
                new Applicant(3, "Cosmo", "Kramer", 100, 568, "a"),
                new Applicant(4, "Elaine", "Benes", 1000000, 788, "a"),
                new Applicant(5, "Susan", "Ross", 10, 788, "a"),
                new Applicant(6, "Jake", "Harper", 34000, 900, "b"),
                new Applicant(7, "John", "Cryer", 12000, 457, "b"),
                new Applicant(8, "Walden", "Schimdt", 10000, 300, "b"),
                new Applicant(9, "Julie", "Dreyfus", 20000, 721, "b"),
                new Applicant(10, "Jason", "Alexandar", 25000, 590, "b")
        );

        SparkConf conf = new SparkConf();
        //Spark session is the main entry point for interfacing with it
        //remember that spark session is 'safe' and can be shared among threads

        SparkSession spark = SparkSession.builder()
                .appName("Simple Application")
                .config(conf)
                .master("local[*]")
                .getOrCreate();

        JavaSparkContext jsc = JavaSparkContext.fromSparkContext(spark.sparkContext());
        KieBase rules = loadRules();

        int nRules = 0;
        Collection<KiePackage> packages = rules.getKiePackages();
        for (KiePackage pack : packages) {
            nRules += pack.getRules().size();
        }
        System.out.printf("Loaded %d rules\n", nRules);

        //broadcast using bittorrent protocol all the rules
        Broadcast<KieBase> broadcastRules = jsc.broadcast(rules);

        //Spark used to parallelize the input data and fire the rules for the rule which is in com/redhat/approval.drl
        Dataset<Applicant> applicantsDS = spark.createDataset(inputData, Encoders.bean(Applicant.class));
        applicantsDS.show(false);

        Dataset<Applicant> drools = applicantsDS
                .map(a -> applyRules(broadcastRules.value(), a), Encoders.bean(Applicant.class))
                .filter(a -> a.isApproved());

        drools.show(false);

        spark.close();
    }

//Load the Rules (approval.drl) in the container 
    public static KieBase loadRules() {
        KieServices kieServices = KieServices.Factory.get();
        KieContainer kieContainer = kieServices.getKieClasspathContainer();
        return kieContainer.getKieBase();
    }

    public static Applicant applyRules(KieBase base, Applicant a) {
        KieSession session = base.newKieSession();
        switch (a.getGroup()) {
            case "a": {
                session.getAgenda().getAgendaGroup("a").setFocus();
                break;
            }
            case "b": {
                session.getAgenda().getAgendaGroup("b").setFocus();
                break;
            }
        }
        session.insert(a);
        session.fireAllRules();
        session.dispose();

        return a;
    }
}
