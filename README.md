# DC Utility

Provides a user interface for generating code lists for publication in the Environment Registry by transforming CSV files to RDF with appropriate structure and metadata.

The underlying template conversion utilities are quite general but this deployment is tied to the assumption on publishing registry code lists. In particular the metadata form is specific to the categorization schemes and owners relevant to the Environment Registry. It also assumes the will fit in memory and can be processed synchronously without requiring long running batch processing services

The UI supports:
   * registration/login via any OpenID provider, such as google
   * upload and preview of CSV source data file to be converted
   * selection (or upload) of a processing template to define the data layout
   * metadata form to describe the resulting code lists - especially owner, license and categories.
   * run the data conversion and explore the resulting RDF data
   * download the converted data for publication to the registry
   

      

