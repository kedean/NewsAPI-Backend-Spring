Spring Backend
==========

Backend for the NewsAPI system built using SpringMVC.

##Data backing
Currently there are three data engines available: in memory, Mongo DB, and Redis.

###In Memory
The in memory backing, or ```in-mem```, stores all data in Java HashMap objects that are discarded between runs of the application. This exists as a portable fallback only, and is not suitable for real usage.

###MongoDB
The Mongo DB backing connects to an arbitrary Mongo server. To use this, set ```repositories.engine.type=mongo```, and ```repositories.engine.host```, ```repositories.engine.port```, and ```repositories.engine.db``` to appropriate values. This will store all stories and previews in your Mongo instance, and preserves the data across runs of the application. At this time, credentials are not supported for Mongo.

###Redis
The Redis backing connects to an arbitrary Redis installation. Set ```repositories.engine.type=redis``` and ```repositories.engine.host``` and ```repositories.engine.port``` to appropriate values. Authentication is not currently supported. The prefix values may conflict with other content stored in redis, so isolated instances work best.
