## Prerequisite

- [Docker](https://docs.docker.com/get-docker/)
- [Leiningen](https://leiningen.org/)

## Run

``` sh
# Start PostgreSQL
docker run --rm --name postgres -p 5432:5432 -e POSTGRES_PASSWORD=pass -d postgres

# Run the HTTP server
lein run

# Run your favorite browser!
http://localhost:9876/
```

## Results

``` json
{
   "title":"Plan d'études",
   "validation":{
      "grade":5.0,
      "ects":60.0,
      "etatValidation":"NON_VALIDE"
   },
   "children":[
      {
         "title":"Module 1",
         "validation":{
            "grade":5.5,
            "ects":30.0,
            "etatValidation":"ACQUIS"
         }
      },
      {
         "title":"Module 2",
         "validation":{
            "grade":4.5,
            "ects":15.0,
            "etatValidation":"ACQUIS"
         }
      },
      {
         "title":"Module 3",
         "validation":{
            "grade":4.5,
            "ects":15.0,
            "etatValidation":"ACQUIS"
         },
         "children":[
            {
               "title":"UE 1",
               "resultats":[
                  {
                     "tries":1,
                     "grade":3.5
                  },
                  {
                     "tries":2,
                     "grade":5.0
                  }
               ],
               "validation":{
                  "grade":5.0,
                  "ects":15.0,
                  "etatValidation":"ACQUIS"
               }
            },
            {
               "title":"UE 2",
               "resultats":[
                  {
                     "tries":1,
                     "grade":2.0
                  },
                  {
                     "tries":2,
                     "grade":3.0
                  }
               ],
               "validation":{
                  "grade":4.0,
                  "ects":15.0,
                  "etatValidation":"ACQUIS"
               }
            },
            {
               "title":"UE 3",
               "resultats":[
                  {
                     "tries":1,
                     "grade":3.5
                  },
                  {
                     "tries":1,
                     "grade":4.0
                  },
                  {
                     "tries":1,
                     "grade":6.0
                  }
               ],
               "validation":{
                  "grade":6.0,
                  "ects":30.0,
                  "etatValidation":"ACQUIS"
               }
            }
         ]
      },
      {
         "title":"Module 4 - Reconnaissance",
         "validation":{
            "grade":5.0,
            "ects":15.0,
            "etatValidation":"ACQUIS"
         }
      },
      {
         "title":"Module 5",
         "validation":{
            "grade":4.0,
            "ects":5.0,
            "etatValidation":"ACQUIS"
         },
         "children":[
            {
               "title":"UE - Dispense",
               "reconnaissance":{
                  "id":1,
                  "type":"DISPENSE",
                  "grade":4.0,
                  "ects":3.0
               },
               "validation":{
                  "grade":4.0,
                  "ects":3.0,
                  "etatValidation":"ACQUIS"
               }
            },
            {
               "title":"UE - Reconnaissance",
               "reconnaissance":{
                  "id":2,
                  "type":"RECONNAISSANCE",
                  "grade":4.5,
                  "ects":3.0
               },
               "validation":{
                  "grade":4.5,
                  "ects":3.0,
                  "etatValidation":"ACQUIS"
               }
            }
         ]
      },
      {
         "title":"Module 6 - Reconnaissance",
         "reconnaissance":{
            "id":3,
            "type":"RECONNAISSANCE",
            "grade":4.5,
            "ects":5.0
         },
         "validation":{
            "grade":4.5,
            "ects":5.0,
            "etatValidation":"ACQUIS"
         },
         "children":[
            {
               "title":"UE 4A - Equivalence",
               "reconnaissance":{
                  "id":4,
                  "type":"EQUIVALENCE",
                  "grade":4.5,
                  "ects":5.0,
                  // Un élément reconnu n'apparaît pas dans l'arbre étudiant mais sous son équivalence.
                  "reconnu":{
                     "title":"UE 4B - Reconnu"
                  }
               },
               "validation":{
                  "grade":4.5,
                  "ects":5.0,
                  "etatValidation":"ACQUIS"
               }
            }
         ]
      }
   ]
}
```
