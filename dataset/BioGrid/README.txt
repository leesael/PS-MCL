Data From BioGrid

The column contents of BioGRID Interaction Tab 2.0 files should be as follows:

BioGRID Interaction ID. 
	A unique identifier for each interaction within the BioGRID database. Can be used to link to BioGRID interaction pages. For example: http://thebiogrid.org/interaction/616539

Entrez Gene ID 
	for Interactor A. The identifier from the Entrez-Gene database that corresponds to Interactor A. If no Entrez Gene ID is available, this will be a “-”.

Entrez Gene ID 
	for Interactor B. Same structure as column 2.

BioGRID ID 
	for Interactor A. The identifier in the BioGRID database that corresponds to Interactor A. These identifiers are best used for creating links to the BioGRID from your own websites or applications. To link to a page within our site, simply append the URL: http://thebiogrid.org/ID/ to each ID. For example, http://thebiogrid.org/31623/.

BioGRID ID 
	for Interactor B. Same structure as column 4.

Systematic name for Interactor A. 
	A plain text systematic name if known for interactor A. Will be a “-” if no name is available.

Systematic name for Interactor B.
	 Same structure as column 6.

Official symbol for Interactor A. 
	A common gene name/official symbol for interactor A. Will be a “-” if no name is available.

Official symbol for Interactor B. 
	Same structure as column 8.

Synonyms/Aliases for Interactor A. 
	A “|” separated list of alternate identifiers for interactor A. Will be “-” if no aliases are available.

Synonyms/Aliases for Interactor B. 
	Same stucture as column 10.

Experimental System Name. 
	One of the many Experimental Evidence Codes supported by the BioGRID.

Experimental System Type. 
	This will be either “physical” or “genetic” as a classification of the Experimental System Name.

First author 
	surname of the publication in which the interaction has been shown, optionally followed by additional indicators, e.g. Stephenson A (2005)

Pubmed ID 
	of the publication in which the interaction has been shown.

Organism ID 
	for Interactor A. This is the NCBI Taxonomy ID for Interactor A.

Organism ID 
	for Interactor B. Same structure as 16.

Interaction Throughput. 
	This will be either High Throughput, Low Throughput or Both (separated by “|”).

Quantitative Score. 
	This will be a positive for negative value recorded by the original publication depicting P-Values, Confidence Score, SGA Score, etc. Will be “-” if no score is reported.

Post Translational Modification. 
	For any Biochemical Activity experiments, this field will be filled with the associated post translational modification. Will be “-” if no modification is reported.

Phenotypes. 
	If any phenotype info is recorded, it will be provided here separated by “|”. Each phenotype will be of the format <phenotype>[<phenotype qualifier>]:<phenotype type>. Note that the phenotype types and qualifiers are optional 
	and will only be present where recorded. Phenotypes may also have multiple qualifiers in which case unique qualifiers will be separated by carat (^). If no phenotype information is available, this field will contain “-”.

Qualifications. 
	If additional plain text information was recorded for an interaction, it will be listed with unique qualifiers separated by “|”. If no qualification is available, this field will contain “-”.
Tags. If an interaction has been tagged with additional classifications, they will be provided in this column separated by “|”. If no tag information is available, this field will contain “-”.
Source Database. This field will contain the name of the database in which this interaction was provided.

