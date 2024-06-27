<template>
  <header>
    <HeaderNav :active-index="1" />
  </header>
  <main class="q-my-lg">
  <div class="q-mx-auto col-8" style="max-width: 1100px">
    <div class="row">
      <FilterSortPanel class="col-md-4 col-12" :getLineColor="getLineColor" @change="updateStatistics" />
      <div class="col-md-8 col-12">
        
        <StatisticChart chartTitle="Statistik" ref="chartPanel" />

        <ChartCarousel v-show="showYearlyChart" ref="chartCarousel"/>

        <StatisticChart chartTitle="Statistik Jahre"
          v-show="showYearlyChart" ref="yearChartPanel" />


      </div>

    </div>
  </div>
</main>

</template>

<script>
/* eslint-disable */
import HeaderNav from '@/components/HeaderNav.vue'
import FilterSortPanel from '@/components/FilterSortPanel.vue'
import StatisticChart from '@/components/StatisticChart.vue'
import ChartCarousel from '@/components/ChartCarousel.vue'



export default {
  name: 'StatisticsView',

  data () {
    return {
      showYearlyChart: false,
      showCarousel: false

    }
  },
  components: {
    HeaderNav,
    FilterSortPanel,
    StatisticChart,
    ChartCarousel
  },

  methods: {
    async updateStatistics (params) {
      this.$refs.chartPanel.setErrorValues()

      const statisticsMap = await this.fetchStatistics(params)

      this.$refs.chartPanel.setChartDataMap(statisticsMap.statistic)
      

      if(statisticsMap.yearStatistic != null){
        this.setYearChartData(statisticsMap.yearStatistic)
      } else{
        this.showYearlyChart = false
      }

      if(statisticsMap.yearStatistic != null) {
        this.showCarousel = true 
        this.$refs.chartCarousel.setMonthByYearChartData(statisticsMap.monthByYearStatistic)
        // Set ChartCarousel Data
      } else {
        this.showCarousel = false
      }

    },

    async fetchStatistics (params) {
      if (params.types.length === 0 || params.lines.length === 0) {
        return {}
      }
      try {
        // date parsing
        const fromDateArr = params.fromDate.split('.')
        const fromDate = `${fromDateArr[2]}-${fromDateArr[1]}-${fromDateArr[0]}`
        const toDateArr = params.toDate.split('.')
        const toDate = `${toDateArr[2]}-${toDateArr[1]}-${toDateArr[0]}`
        let url = `http://localhost:5000/statistics?from=${fromDate}&to=${toDate}&${params.sort.value}type=${params.types.toString()}&line=${params.lines.toString()}`
        console.log(url);
        if (params.onlyOpenDisturbances) {
          url += '&active=false'
        }

        // WICHTIG!!!!! Auf Server-gehosteten Backend unnötig
        const res = await fetch(url)
        const data = await res.json()
        if (!('error' in data)) {
          return data
        }
      } catch (err) {
        console.log(err)
      }
      return {}
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

    setYearChartData (yearStatisticMap) {
      if(yearStatisticMap !== null){
        const xAxisData = Object.keys(yearStatisticMap);
        const yAxisData = Object.values(yearStatisticMap);
        this.showYearlyChart = true 
        this.$refs.yearChartPanel.setChartData(xAxisData, yAxisData)
      } else {
        this.showYearlyChart = false
      }
    }
  }
}
</script>

<style>

</style>
