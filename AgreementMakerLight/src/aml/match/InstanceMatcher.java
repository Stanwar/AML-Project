package aml.match;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.sun.jndi.toolkit.url.Uri;

import aml.AML;
import aml.ontology.Ontology;
import aml.ontology.Ontology2Match;
import aml.ontology.RelationshipMap;
import aml.ontology.URIMap;
import aml.util.ISub;
import aml.util.Similarity;
import aml.util.WordNet;


public class InstanceMatcher {
	// GET REFERENCES OF THE GLOBAL 
	private AML aml;
	private Ontology2Match source;
	private Ontology2Match target;
	private RelationshipMap rm;
	private URIMap uris;
	private WordNet wn = null; // From Property Matcher
	
	public InstanceMatcher(){
		aml = AML.getInstance();
		source = aml.getSource();
		target = aml.getTarget();
		
	}
	public Alignment match() throws OWLOntologyCreationException  {
	    	// Code added for Instance Matching 
		System.out.println("Inside instance Matcher Program");
		rm = aml.getRelationshipMap();
		Alignment a = new Alignment();
		
		//OWLAnnotationProperty label = factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
		//GET CLASS NAMES-- PUBLICATION AND PERSON

		Iterator itr2 = source.getIndividuals().iterator();
		
		System.out.println(source.individualCount());
		System.out.println(target.individualCount());
		
		Iterator tgt2 = target.getIndividuals().iterator();
		// HASHMAP FOR SOURCE PUBLICATION 
		HashMap<Integer,ArrayList> pSourceMap = new HashMap<>();
		// HASHMAP FOR TARGET PUBLICATION 
		HashMap<Integer,ArrayList> pTargetMap = new HashMap<>();
		
		int iter = 0;
		while(itr2.hasNext()){	
			int i = (int) itr2.next();
			
			Set<String> src_citations = source.getIndividual(i).getDataValue(4); // DATA PROPERTY FOR CITATIONS
			Set<String> src_title = source.getIndividual(i).getDataValue(8); // DATA PROPERTY FOR TITLE 
			Set<String> src_venue = source.getIndividual(i).getDataValue(9); // DATA PROPERTY FOR VENUE
			Set<String> src_publisher = source.getIndividual(i).getDataValue(11); // DATA PROPERTY FOR PUBLISHER
			Set<String> src_year = source.getIndividual(i).getDataValue(13); // DATA PROPERTY FOR YEAR
			
			// CREATING ARRAY LIST OF ALL THE FIVE FEATURES OF EACH PUBLICATION
			
			ArrayList ar = new ArrayList();
			
			if(src_citations != null){
				ar.add(src_citations.toString());
			}else{
				ar.add("XX");
			}
			
			if(src_title != null ){
				ar.add(src_title.toString());
			}else{
				ar.add("XX");
			}
			
			if(src_venue != null){
				ar.add(src_venue.toString());
			}else{
				ar.add("XX");
			}
			
			if(src_publisher != null){
				ar.add(src_publisher.toString());
			}else{
				ar.add("XX");
			}
			
			if(src_year != null){
				ar.add(src_year.toString());
			}else{
				ar.add("XX");
			}
			
			iter = iter + 1;
			if(!(src_citations == null && src_title == null && src_venue == null && src_publisher == null && src_year == null) ){
				// ADDING EACH PUBLICATION IN SOURCE TO A HASHMAP
				pSourceMap.put(i, ar);
			}
		}
		iter = 0;
		while(tgt2.hasNext()){
			
			int j = (int) tgt2.next();
			Set<String> tgt_citations = target.getIndividual(j).getDataValue(4); // DATA PROPERTY FOR CITATION
			Set<String> tgt_title = target.getIndividual(j).getDataValue(8); // DATA PROPERTY FOR TITLE
			Set<String> tgt_venue = target.getIndividual(j).getDataValue(9); // DATA PROPERTY FOR VENUE
			Set<String> tgt_publisher = target.getIndividual(j).getDataValue(11); // DATA PROPERTY FOR PUBLISHER
			Set<String> tgt_year = target.getIndividual(j).getDataValue(13); // DATA PROPERTY FOR YEAR

			// CREATING ARRAY LIST OF ALL THE FIVE FEATURES OF EACH PUBLICATION
			ArrayList ar2 = new ArrayList();
			if(tgt_citations != null){
				ar2.add(tgt_citations.toString());
			}else{
				ar2.add("XX");
			}
			
			if(tgt_title!=null){
				ar2.add(tgt_title.toString());
			}else{
				ar2.add("XX");
			}
			
			if(tgt_venue != null){
				ar2.add(tgt_venue.toString());
			}else{
				ar2.add("XX");
			}
			
			if(tgt_publisher != null){
				ar2.add(tgt_publisher.toString());
			}else{
				ar2.add("XX");
			}
				
			if(tgt_year != null){
				ar2.add(tgt_year.toString());
			}else{
				ar2.add("XX");
			}
			iter = iter + 1;
			if(!(tgt_year == null && tgt_citations == null && tgt_title==null && tgt_venue == null && tgt_publisher == null)){
				// ADDING EACH PUBLICATION IN TARGET TO A HASHMAP
					pTargetMap.put(j, ar2);
			}
			
		}
		// LOCAL VARIABLES 
		int count = 0;
		int count2 = 0 ;
		int success = 0,failed = 0;
		int loop = 0;
		int second =0;
		int first = 0;
		double maxSim = 0.0 , maxInitial = 0.0;
		String sAuthor = "";
		String tAuthor = "";
		// HASHMAPS FOR CAPTURING MAPPINGS
		HashMap<String, Double> alignScore = new HashMap<String, Double>();
		HashMap<String, Double> aSecondScore = new HashMap<String, Double>();
		HashMap<Integer, ArrayList> secondScore = new HashMap<Integer, ArrayList>();
		
		HashMap<String, Integer> authorNames = new HashMap<String, Integer>();
		int iterate = 0;
		Iterator entry = pSourceMap.entrySet().iterator();
		int cnt = 0;
		int sAuthorID;
		while(entry.hasNext()){
			 Map.Entry pair = (Map.Entry)entry.next();
			 ArrayList src_map = (ArrayList) pair.getValue();
		    int skey = (int) pair.getKey();
		    count2 = count2 + 1;

			loop = loop + 1;
			maxSim = 0.0;
			maxInitial = 0.0;
			sAuthor = "";
			tAuthor = "";
			sAuthorID = 0;
			for(Map.Entry<Integer, ArrayList> tentry : pTargetMap.entrySet()){
				
				int tkey = tentry.getKey();
			    ArrayList tgt_map = tentry.getValue();
			    
			    // CHECK FOR EACH PUBLICATION'S YEAR AND PUBLISHER
				if ( src_map.get(4).equals(tgt_map.get(4))
						&& src_map.get(3).equals(tgt_map.get(3))
						&& src_map.get(2).toString().equalsIgnoreCase("XX") && tgt_map.get(3).toString().equalsIgnoreCase("XX")
						){
					
					// GET EACH PUBLICATION'S TITLE
					String s = src_map.get(1).toString();
					String t = tgt_map.get(1).toString();
					
					// MATCHING TITLE -- DONE TO IMPROVE PERFORMANCE
					if(src_map.get(1).equals(tgt_map.get(1))){
						first = first + 1; 
						double sim = nameSimilarity(s,t);
						
						// TAKING THE MAXIMUM SIM SCORE FOR SOURCE'S PUBLICATION  
						if(maxInitial < sim){
							maxInitial = sim; 
						}
						
						try{
							//  TAKING THRESHOLD 
							if(maxInitial > 0.80){
								// CALCULATING THE AUTHOR ID FOR TARGET AND SOURCE
								// AND THEN GETTING THE NAMES EACH AUTHOR
								Set<Integer> target_author_id = rm.getChildrenIndividuals(tkey, 14);
								Iterator tID = target_author_id.iterator();
								int id = 0;
								while(tID.hasNext()){
									id = (int) tID.next();
								}
								
								String target_author_name = "";
								if(id != 0 ){
									target_author_name = target.getIndividual(id).getDataValue(6).toString();
								}
								Set<Integer> source_author_id = rm.getChildrenIndividuals(skey,14);
								Iterator sID = source_author_id.iterator();
								int id2 =0;
								while(sID.hasNext()){
									id2 = (int) sID.next();
								}
								String source_author_name = "";
								
								if(id2!=0){
									source_author_name = source.getIndividual(id2).getDataValue(6).toString();
								}

								if(id2 == 20454){
									System.out.println("CHECK ");
								}
								/// COMPARING AUTHOR NAMES
								double innersim = nameSimilarity(source_author_name,target_author_name);
								// THRESHOLD2 
								if(innersim > 0.50){
									String src = source.getIndividual(id2).getName();
									String tgt = target.getIndividual(id).getName();
									
									uris = aml.getURIMap();
									src = uris.getURI(id2);
									tgt = uris.getURI(id);	
									// MAXIMUM SIMILARITY SCORE. 
									maxSim = innersim;
									
									sAuthor = src;
									tAuthor = tgt;
									sAuthorID = skey;
								}
								
							}
						}
						catch(Exception ex){
							System.out.println("Exception 2: " + ex);
						}
					}
					
					
				}
				else if(src_map.get(4).equals(tgt_map.get(4))
						&& src_map.get(3).equals(tgt_map.get(3))
						&& src_map.get(2).equals(tgt_map.get(2))
						&& src_map.get(0).equals(tgt_map.get(0))
						){
					second = second + 1;
					/// MATCHING THOSE PUBLICATIONS WHICH HAVE 4 FEATURES MATCHING 
					String s = src_map.get(1).toString();
					String t = tgt_map.get(1).toString();
					
					double sim = nameSimilarity(s,t);
					if(maxInitial < sim){
						maxInitial = sim;
					}
					try{
						if(maxInitial > 0.80){
							
							Set<Integer> target_author_id = rm.getChildrenIndividuals(tkey, 14);
							
							Iterator tID = target_author_id.iterator();
							
							int id = 0;
							while(tID.hasNext()){
								id = (int) tID.next();
							}
							
							String target_author_name = "";
							if(id != 0 ){
								target_author_name = target.getIndividual(id).getDataValue(6).toString();
							}
							Set<Integer> source_author_id = rm.getChildrenIndividuals(skey,14);
							Iterator sID = source_author_id.iterator();
							int id2 =0;
							while(sID.hasNext()){
								id2 = (int) sID.next();
							}
							String source_author_name = "";
							
							if(id2!=0){
								source_author_name = source.getIndividual(id2).getDataValue(6).toString();
							}
							double innersim = nameSimilarity(source_author_name,target_author_name);
							if(innersim > 0.50){
								String src = source.getIndividual(id2).getName();
								String tgt = target.getIndividual(id).getName();
								sAuthorID = id2;
								uris = aml.getURIMap();
								src = uris.getURI(id2);
								tgt = uris.getURI(id);	
									maxSim = innersim;
									sAuthor = src;
									tAuthor = tgt;
									sAuthorID = skey;
							}
							
						}
					}
					catch(Exception ex){
						System.out.println("Exception 3: " + ex);
					}
				}
				
				
			}
			// FINAL THRESHOLD PUT 
			if(maxSim >= 0.50){
				
				// IF THE TARGET AUTHOR DOESNT EXIST, TAKE THE FIRST MAPPING AND ADD TO HASHMAP
				// ADD THE SOURCE-AUTHOR AND THE TARGET AUTHOR TO A NEW HASHMAP
				if(!authorNames.containsKey(tAuthor)){
					
					authorNames.put(tAuthor, 1);
					String index = sAuthor + "|" + tAuthor;
					alignScore.put(index, maxSim);
					
				}
				else if (authorNames.containsKey(tAuthor)){
					// PUT ALL THE OTHER MATCHED MAPPINGS IN ANOTHER HASHMAP TO BE CHECKED AGAINST THE TARGET ONTOLOGY
					String index = sAuthor + "|" + tAuthor;
					Set<String> scitations = source.getIndividual(sAuthorID).getDataValue(4);
					Set<String> stitle = source.getIndividual(sAuthorID).getDataValue(8);
					Set<String> svenue = source.getIndividual(sAuthorID).getDataValue(9);
					Set<String> spublisher = source.getIndividual(sAuthorID).getDataValue(11);
					Set<String> syear = source.getIndividual(sAuthorID).getDataValue(13);
					
					
					ArrayList ar2 = new ArrayList();
					
					if(scitations != null){
						ar2.add(scitations.toString());
					}else{
						ar2.add("XX");
					}
					
					if(stitle!=null){
						ar2.add(stitle.toString());
					}else{
						ar2.add("XX");
					}
					
					if(svenue != null){
						ar2.add(svenue.toString());
					}else{
						ar2.add("XX");
					}
					
					if(spublisher != null){
						ar2.add(spublisher.toString());
					}else{
						ar2.add("XX");
					}
						
					if(syear != null){
						ar2.add(syear.toString());
					}else{
						ar2.add("XX");
					}
					
					secondScore.put(sAuthorID, ar2);
				}
				
			}
			entry.remove();

		}
		
		ArrayList<String> ar = new ArrayList<String>();
		// SEND ALL THE MAPPINGS TO AN ALIGNMENT 
		for(Map.Entry<String,Double> fentry : alignScore.entrySet()){
			
			String key = fentry.getKey();
			double val = fentry.getValue();
			String src = key.substring(0, key.indexOf("|"));
			String tgt = key.substring(key.indexOf("|") + 1);
			// ADDING TO ALIGNMENT
			a.add(src,tgt,1.0);
			ar.add(src.toString());
			success = success + 1;
		}
		
		//// 
		// CHECKING THE MAPPINGS MISSED OUT IN THE FIRST RUN AGAINST THE TARGET ONTOLOGY 
		///
		int ggg = 0, ct = 0;
		HashMap<String,Integer> ANS = new HashMap<String, Integer>();
		Iterator ientry = secondScore.entrySet().iterator();
		while(ientry.hasNext()){
			 Map.Entry pair = (Map.Entry)ientry.next();
			 ArrayList src_map = (ArrayList) pair.getValue();
		    int skey = (int) pair.getKey();
		    
		    count2 = count2 + 1;

			loop = loop + 1;
			maxSim = 0.0;
			maxInitial = 0.0;
			sAuthor = "";
			tAuthor = "";
			
			for(Map.Entry<Integer, ArrayList> tentry : pTargetMap.entrySet()){
				
				int tkey = tentry.getKey();
			    ArrayList tgt_map = tentry.getValue();
				count = count + 1;
				
				if(src_map.get(1).equals(tgt_map.get(1))){
					String s = src_map.get(1).toString();
					String t = tgt_map.get(1).toString();
					first = first + 1; 
					double sim = nameSimilarity(s,t);
					if(maxInitial < sim){
						maxInitial = sim; 
					}
					if(maxInitial > 0.80){
						Set<Integer> target_author_id = rm.getChildrenIndividuals(tkey, 14);
						Iterator tID = target_author_id.iterator();
						int id = 0;
						while(tID.hasNext()){
							id = (int) tID.next();
						}
						
						String target_author_name = "";
						if(id != 0 ){
							target_author_name = target.getIndividual(id).getDataValue(6).toString();
						}
						Set<Integer> source_author_id = rm.getChildrenIndividuals(skey,14);
						Iterator sID = source_author_id.iterator();
						int id2 =0;
						while(sID.hasNext()){
							id2 = (int) sID.next();
						}
						String source_author_name = "";
						
						if(id2!=0){
							source_author_name = source.getIndividual(id2).getDataValue(6).toString();
						}
						double innersim = nameSimilarity(source_author_name,target_author_name);
						if(innersim > 0.50){
							String src = source.getIndividual(id2).getName();
							String tgt = target.getIndividual(id).getName();
							uris = aml.getURIMap();
							src = uris.getURI(id2);
							tgt = uris.getURI(id);	
							
							maxSim = innersim;
							
							sAuthor = src;
							tAuthor = tgt;
						}
						
					}
				}
			}
			// THRESHOLD FOR THE SECOND RUN 
			if(maxSim >= 0.70){
				
				if(!ANS.containsKey(tAuthor)){
					
					ANS.put(tAuthor, 1);
					String index = sAuthor + "|" + tAuthor;
					//System.out.println("index : " + index);
					aSecondScore.put(index, maxSim);
					
				}
			}
			ientry.remove();

		}
		int size = aSecondScore.size();
		int varia = 0;
		// ADDING THE MAPPINGS OF THE SECOND RUN TO THE ALIGNMENT 
		for(Map.Entry<String,Double> fentry : aSecondScore.entrySet()){
			
			String key = fentry.getKey();
			double val = fentry.getValue();
			String src = key.substring(0, key.indexOf("|"));
			String tgt = key.substring(key.indexOf("|") + 1);
			
			a.add(src,tgt,1.0);
		}
		System.out.println("First : " + first);
		System.out.println("Hashmap Val : " + alignScore.size());
		System.out.println(" Success : " + success);
		System.out.println(" failed : " + failed);
		System.out.println(" Second : " + second);
		System.out.println(" SourceSize : " + pSourceMap.size());
		System.out.println(" TargetSize : " + pTargetMap.size());
		System.out.println("the count : " + count);
		System.out.println("the count2 : " + count2);
		//System.out.println(a.size());
		return a;
	}
	//////
	// PROPERTY MATCHER FROM AML  
	/////
	private double nameSimilarity(String s, String t)
	{
		String sourceNames = s;
		String targetNames = t;
		
		if(sourceNames.equalsIgnoreCase(targetNames))
			return 1.0;

		double sim = 0.0;
		double newSim = nameSimilarity(sourceNames,targetNames,wn != null);
		if(newSim > sim)
			sim = newSim;
			
		return sim;
	}
	private double nameSimilarity(String n1, String n2, boolean useWordNet)
	{
		//Check if the names are equal
		if(n1.equals(n2))
			return 1.0;
		
		//Split the source name into words
		String[] sW = n1.split(" ");
		HashSet<String> sWords = new HashSet<String>();
		HashSet<String> sSyns = new HashSet<String>();
		for(String w : sW)
		{
			sWords.add(w);
			sSyns.add(w);
			//And compute the WordNet synonyms of each word
			if(useWordNet && w.length() > 2)
				sSyns.addAll(wn.getAllNounWordForms(w));
		}
		
		//Split the target name into words
		String[] tW = n2.split(" ");
		HashSet<String> tWords = new HashSet<String>();
		HashSet<String> tSyns = new HashSet<String>();		
		for(String w : tW)
		{
			tWords.add(w);
			tSyns.add(w);
			//And compute the WordNet synonyms of each word
			if(useWordNet && w.length() > 3)
				tSyns.addAll(wn.getAllWordForms(w));
		}
		
		//Compute the Jaccard word similarity between the properties
		double wordSim = Similarity.jaccard(sWords,tWords)*0.9;
		//and the String similarity
		double simString = ISub.stringSimilarity(n1,n2)*0.9;
		//Combine the two
		double sim = 1 - ((1-wordSim) * (1-simString));
		if(useWordNet)
		{
			//Check if the WordNet similarity
			double wordNetSim = Similarity.jaccard(sSyns,tSyns);
			//Is greater than the name similarity
			if(wordNetSim > sim)
				//And if so, return it
				sim = wordNetSim;
		}
		return sim;
	}

	//Checks if two lists of values match (i.e., have Jaccard similarity above 50%)
	private boolean valuesMatch(Set<String> sRange, Set<String> tRange)
	{
		if(sRange.size() == 0 && tRange.size() == 0)
			return true;
		if(sRange.size() == 0 || tRange.size() == 0)
			return false;
		double sim = Similarity.jaccard(sRange,tRange);
		return (sim > 0.5);
	}
}
