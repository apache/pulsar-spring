# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

CQL="CREATE KEYSPACE IF NOT EXISTS sample_pulsar_functions_keyspace WITH replication = {'class':'SimpleStrategy', 'replication_factor':1};
USE sample_pulsar_functions_keyspace;
CREATE TABLE customer_onboard (customer_email text PRIMARY KEY, customer_details text);"

until echo $CQL | cqlsh; do
  echo "cqlsh: Cassandra is unavailable to initialize - will retry later"
  sleep 20
done &

exec /usr/local/bin/docker-entrypoint.sh "$@"
