<template>
  <div>
    <input v-model="cypherQuery" placeholder="Enter Cypher query" />
    <button @click="executeQuery">Execute</button>
    <div v-if="results">
      <h3>Results:</h3>
      <pre>{{ results }}</pre>
    </div>
  </div>
</template>

<script>
import neo4j from 'neo4j-driver';

export default {
  name: 'Neo4j',
  data() {
    return {
      cypherQuery: 'MATCH p=()-->() RETURN p LIMIT 25',
      results: null,
      driver: null
    };
  },
  created() {
    this.driver = neo4j.driver(
        'bolt://localhost:7687',
        neo4j.auth.basic('neo4j', 'zwb052116')
    );
  },
  beforeDestroy() {
    if (this.driver) {
      this.driver.close();
    }
  },
  methods: {
    async executeQuery() {
      const session = this.driver.session();
      try {
        const result = await session.run(this.cypherQuery);
        this.results = result.records.map(record => record.toObject());
      } catch (error) {
        console.error('错误:', error);
        this.results = error;
      } finally {
        await session.close();
      }
    }
  }
};
</script>
