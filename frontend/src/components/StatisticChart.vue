<template>
    <div class="q-pa-md ">
        <div class="bg-white shadow-4 rounded-borders q-pa-md">

            <h2>Statistik</h2>
            <div v-if="loading" class="row justify-center">
                <q-spinner size="md" color="primary" />
            </div>

            <div v-else>
                <apexchart type="bar" :options="options" :series="series"></apexchart>
            </div>
        </div>
    </div>
</template>

<script>
import VueApexCharts from 'vue3-apexcharts'

export default {
  name: 'StatisticChart',
  components: {
    apexchart: VueApexCharts
  },
  methods: {
    async update (params) {
      this.loading = true
      const statisticsMap = await this.fetchStatistics(params)
      const xAxisData = await statisticsMap.map((xyUnit) => this.getCategoryString(xyUnit.month))
      const yAxisData = await statisticsMap.map((xyUnit) => xyUnit.amountDisturbances)

      this.options = {
        ...this.options,
        xaxis: {
          categories: xAxisData
        }
      }

      this.series[0].data = yAxisData

      this.loading = false
    },

    getCategoryString (monthkey) {
      const monthsArray = ['Januar', 'Februar', 'März', 'April', 'Mai', 'Juni', 'Juli', 'August', 'September', 'Oktober', 'November', 'Dezember']
      const monthKeyStr = String(monthkey)
      const monthInt = monthKeyStr.substring(4, 6)
      const yearString = monthKeyStr.replace(monthInt, '')

      // Index entsprechend des Monats
      const monthString = monthsArray[Number(monthInt) - 1]

      return `${monthString} ${yearString}`
    },

    async fetchStatistics (params) {
      if (params.types.length === 0 || params.lines.length === 0) {
        return []
      }
      try {
        // date parsing
        const fromDateArr = params.fromDate.split('.')
        const fromDate = `${fromDateArr[2]}-${fromDateArr[1]}-${fromDateArr[0]}`
        const toDateArr = params.toDate.split('.')
        const toDate = `${toDateArr[2]}-${toDateArr[1]}-${toDateArr[0]}`
        let url = `http://0.0.0.0:5000/statistics?from=${fromDate}&to=${toDate}&${params.sort.value}type=${params.types.toString()}&line=${params.lines.toString()}`

        console.log(url)
        if (params.onlyOpenDisturbances) {
          url += '&active=true'
        }

        // WICHTIG!!!!! Auf Server-gehosteten Backend unnötig
        const res = await fetch(url)
        const data = await res.json()
        console.table(data.statistic)
        if (!('error' in data)) {
          return data.statistic
        }
      } catch (err) {
        console.log(err)
      }
      return []
    },


    /**
     * Überdenken der Response:
     * Wenn für mehr als 2 Jahre gefiltert wird,
     * dann soll ein Array von YearData Objekten zurückgegeben
     * und ein Array von Arrays, welche die MonthData Objekte speichern. 
     * Jedes dieser Array hat einen Jahres Identifikator 
     */
    checkMultipleYears(monthList){

        let differentYears = 0;
        var yearList = []
        monthList.forEach(element => {
            
            const yearString = element.substring(0, 4)
            parseInt(yearString)

        });
    }
  },

  data () {
    return {
      loading: true,

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
      }]
    }
  }
}
</script>

<style>

</style>
