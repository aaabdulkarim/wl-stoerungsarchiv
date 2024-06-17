<template>
    <div class="q-pa-md ">
        <div class="bg-white shadow-4 rounded-borders q-pa-md">

            <h2>{{ chartName }}</h2>
            <div v-if="loading" class="row justify-center">
                <q-spinner size="md" color="primary" />
            </div>

            <div v-else-if="noValues">
                <h3>
                  Keine Störungen für diesen Zeitraum
                </h3>
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
    chartName : String
  },
  components: {
    apexchart: VueApexCharts
  },
  methods: {
    /**
     * Diese Methode wird benutzt um die Daten direkt zu setzen. 
     * @param {*} xAxisData 
     * @param {*} yAxisData 
     */
    setChartData(xAxisData, yAxisData) {
      console.log("Triggered ");
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
    async update (params) {
      this.loading = true;
      this.noValues = true;

      const statisticsMap = await this.fetchStatistics(params)
      let checkMultipleYearsResult = null;

      // From Date verarbeiten
      let [dayFrom, monthFrom, yearFrom] = params.fromDate.split('.').map(Number);
      
      // To Date verarbeiten
      let [dayTo, monthTo, yearTo] = params.toDate.split('.').map(Number);
      

      if(yearTo - yearFrom >= 2){
        checkMultipleYearsResult = this.checkMultipleYears(statisticsMap)
      }
      this.$emit("multipleYears", checkMultipleYearsResult)
      
      
      const xAxisData = await statisticsMap.map((xyUnit) => this.getCategoryString(xyUnit.month))
      const yAxisData = await statisticsMap.map((xyUnit) => xyUnit.amountDisturbances)
      
      if(yAxisData.length == 1 && yAxisData[0]==0){
        this.noValues = true;
        this.loading = false
      } else {
        this.setChartData(xAxisData, yAxisData)
      }
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

        if (params.onlyOpenDisturbances) {
          url += '&active=false'
        }

        // WICHTIG!!!!! Auf Server-gehosteten Backend unnötig
        const res = await fetch(url)
        const data = await res.json()
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
     *
     */
    checkMultipleYears (statisticsMap) {
      // Wird bestehen aus einem Array von {"yearString": amountDisturbances} Objekten
      let statisticsYearly = []
      let differentYears = -1

      const years = []
      const disturbances = []

      statisticsMap.forEach(element => {
        const yearString = String(element.month).substring(0, 4)
        const amountDisturbance = element.amountDisturbances

        if (!years.includes(yearString)) {
          years.push(yearString)
          disturbances.push(amountDisturbance)
          differentYears++
        } else {
          disturbances[differentYears] += amountDisturbance
        }
      })

      statisticsYearly = years.map((year, index) => {
        return { yearString: year, amountDisturbances: disturbances[index] }
      })

      console.log(differentYears)
      if (differentYears <= 1) return null
      else return {years: years, amountDisturbances: disturbances}
    }

  }, 
}
</script>

<style>

</style>
