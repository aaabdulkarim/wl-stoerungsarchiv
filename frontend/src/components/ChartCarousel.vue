<template>
    <div class="q-pa-md ">
        <!-- <div class="bg-white shadow-4 rounded-borders q-pa-md"> -->

            <q-carousel
        v-model="slide"
        transition-prev="scale"
        transition-next="scale"
        swipeable
        animated
        control-color="primary"
        navigation
        padding
        arrows
        height="560px"
        class="custom-carousel"
      >
          <!-- Bei Charts wo alle Jahre angezeigt werden, werden die Labels nicht gescheit angezeigt
           Wenn man das Fenster resized passt sich die größe an -->
        <q-carousel-slide
          v-for="(chartTitle, index) in statisticChartData.chartTitles"
          :key="index"
          :name="index"
          class="slide-carousel"
        >
          <StatisticChart
            :chartTitle="chartTitle + ' Übersicht'"
            :chartMapData="statisticChartData.chartDataMaps[index]"
          />
        </q-carousel-slide>
      </q-carousel>

        <!-- </div> -->
    </div>
</template>

<script>
import StatisticChart from './StatisticChart.vue'
/* eslint-disable */

export default {
    name: 'ChartCarousel',
    data() {
        return {
            slide: 0,
            statisticChartData: {
                chartTitles: [],
                chartDataMaps: []

            }
        }
    },
    components: {
        StatisticChart,
    },
    methods: {
      setMonthByYearChartData(monthByYearStatistic){
        this.statisticChartData = {
            chartTitles : Object.keys(monthByYearStatistic),
            chartDataMaps : Object.values(monthByYearStatistic)
        }
      }
    }
}
</script>

<style>
.custom-carousel {
  background-color: transparent !important;
}

.slide-carousel {
}
</style>
