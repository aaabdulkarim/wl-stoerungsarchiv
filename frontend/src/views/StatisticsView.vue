<template>
  <header>
    <HeaderNav :active-index="1" />
  </header>
  <main class="q-my-lg">
    <div class="q-mx-auto col-8 row" style="max-width: 1100px">
      <FilterSortPanel class="col-md-4 col-12" :getLineColor="getLineColor" @change="updateStatistics" />
      <StatisticChart class="col-md-8 col-12" chartName="Statistik"
        @multipleYears="setYearChartData" ref="chartPanel" />
      <StatisticChart class="col-md-8 col-12" chartName="Statistik Jahre"
        v-show="showYearlyChart" ref="yearChartPanel" />
    </div>
  </main>
</template>

<script>
/* eslint-disable */
import HeaderNav from '@/components/HeaderNav.vue'
import FilterSortPanel from '@/components/FilterSortPanel.vue'
import StatisticChart from '@/components/StatisticChart.vue'

export default {
  name: 'StatisticsView',

  components: {
    HeaderNav,
    FilterSortPanel,
    StatisticChart
  },
  methods: {
    updateStatistics (params) {
      this.$refs.chartPanel.update(params)
    },

    getLineColor (id, type) {
      const colors = ['blue', 'red', 'grey', 'purple']
      if (type === 2) {
        if (id.includes('U1')) { return 'red' }
        if (id.includes('U2')) { return 'pink' }
        if (id.includes('U3')) { return 'orange' }
        if (id.includes('U4')) { return 'green' }
        if (id.includes('U5')) { return 'teal' }
        if (id.includes('U6')) { return 'brown' }
      }
      return colors[type]
    },

    setYearChartData (checkMultipleYearsResult) {
      if(checkMultipleYearsResult !== null){
        this.showYearlyChart = true 
        this.$refs.yearChartPanel.setChartData(checkMultipleYearsResult.years, checkMultipleYearsResult.amountDisturbances)
      } else {
        this.showYearlyChart = false
      }
    }
  },

  data () {
    return {
      showYearlyChart: false
    }
  }
}
</script>

<style>

</style>
