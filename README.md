# AML-Project

Adding OAEI's Instance Matching track to Agreement Maker Light was the final project in **CS 586 : Data and Web Semantics** under the guidance of **Prof. Isabel Cruz** . **UIC's** AML has never participated in Instance Matching track in OAEI's competition and the work and results are unprecedented. 

Details about the project : 
- The task of the project is to disambiguate authors using their publications. Comparing publications from source ontology to the target ontology using the different features of each ontology. 
  The features for each publication are as follows : 
  1. Title
  2. Venue
  3. Publisher
  4. Citations
  5. Year
  
- Sandbox Ontologies had about 28k and 14k in Source and Target Ontologies. 
- Reference Alignment had about 854 final mappings for authors.

Techniques applied : 
- Property Matcher technique was applied which uses the following techniques
  1. Structural Similarity 
  2. TF-IDF
  3. Jaccard Similarity Score
  4. Wordnet Similarity using JAWS API
  
Future Work and Improvements : 
- Some of the authors have publications as co-authors and have matching last names but without a first name. This has to be disambiguated using checking the number of publications for each author. 

### Results :

- **Precision : 90.6%**
- **Recall : 94.4%** 
- **F-Measure : 92.4%**

More information about the competition and the associated papers can be found at : 
#### http://www.om2015.ontologymatching.org/

More information about the instance matching track along with the data can be found at : 
#### http://islab.di.unimi.it/im_oaei_2015/index.html
