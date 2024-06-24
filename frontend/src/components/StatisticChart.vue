<template>
    <div class="q-pa-md ">
        <div class="bg-white shadow-4 rounded-borders q-pa-md">

            <h2>{{ chartTitle }}</h2>
            <div v-if="loading" class="row justify-center">
                <q-spinner size="md" color="primary" />
            </div>

            <div v-else-if="noValues">
                <div class="text-center text-grey">
                  Keine Störungen passend zum gesetzten Filter gefunden
                </div>
            </div>

            <div v-else-if="!noValues || !loading">
                <apexchart type="bar" :options="options" :series="series"></apexchart>
            </div>
        </div>
    </div>
</template>

<script>
/* eslint-disable */
import VueApexCharts from 'vue3-apexcharts'

export default {
  name: 'StatisticChart',
  data () {
    return {
      loading: true,
      noValues: false,

      options: {

        plotOptions: {
          bar: {
            distributed: false
          }
        },
        colors:
                    ['#e30014'],
        dataLabels: {
          enabled: false
        },
        legend: {
          show: false
        },
        chart: {
          id: 'statistics-chart'
        },
        xaxis: {
          categories: []
        },
        // Damit keine Nachkommazahlen dargestellt werden
        yaxis: {
          labels: {
            formatter: function (value) {
              return parseInt(value)
            }
          }
        }
      },
      series: [{
        name: 'statistics',
        data: []
      }],
      
    }
  },

  props: {
    chartTitle : String,
    xAxisData : Array,
    yAxisData : Array
  },
  components: {
    apexchart: VueApexCharts
  },
  mounted() {
    if(this.$props.xAxisData !== undefined && this.$props.yAxisData !== undefined){
      this.setChartData(this.$props.xAxisData, this.$props.yAxisData)
    }
  },
  methods: {
    /**
     * Diese Methode wird benutzt um die Daten direkt zu setzen. 
     * @param {*} xAxisData 
     * @param {*} yAxisData 
     */
    setChartData(xAxisData, yAxisData) {
      this.options = {
        ...this.options,
        xaxis: {
          categories: xAxisData
        }
      }

      this.series[0].data = yAxisData;
      this.loading = false;
      this.noValues = false;

    },

    setChartDataMap(statisticsMap) {

      const xAxisData = statisticsMap.map((xyUnit) => this.getCategoryString(xyUnit.month))
      const yAxisData = statisticsMap.map((xyUnit) => xyUnit.amountDisturbances)
      
      if(yAxisData.length == 0){
        this.noValues = true;
        this.loading = false;
      } else {
        this.setChartData(xAxisData, yAxisData)
      }

    },


    setErrorValues () {
      this.loading = true;
      this.noValues = true;
    },

    getCategoryString (monthkey) {
      const monthsArray = ['Januar', 'Februar', 'März', 'April', 'Mai', 'Juni', 'Juli', 'August', 'September', 'Oktober', 'November', 'Dezember']
      const monthKeyStr = String(monthkey)
      const monthInt = monthKeyStr.substring(4, 6)
      const yearString = monthKeyStr.substring(0, 4)
      
      // Index entsprechend des Monats
      const monthString = monthsArray[Number(monthInt) - 1]

      return `${monthString} ${yearString}`
    },



  }, 
}
</script>

<style>

</style>
