<template>
    <div class="q-pa-md ">
        <div class="bg-white shadow-4 rounded-borders q-pa-md">

            <q-carousel v-model="slide"
                transition-prev="scale"
                transition-next="scale"
                swipeable animated
                control-color="white" navigation padding arrows height="300px"
                class="bg-primary text-white shadow-1 rounded-borders">

                <q-carousel-slide v-for="(statisticObject, index) in statisticArray" :key="index" :name="index">
                    <StatisticChart :chartTitle="statisticObject.chartName"
                        :xAxisData="statisticObject.xAxisData"
                        :yAxisData="statisticObject.yAxisData" />
                </q-carousel-slide>
            </q-carousel>

        </div>
    </div>
</template>

<script>
import StatisticChart from './StatisticChart.vue'

/* eslint-disable */

export default {
    name: 'ChartCarousel',
    data() {
        return {
            slide: 0
        }
    },

    props: {
        // Array aus Objekten: {chartTitle, xaxisdata, yaxisdata}
        statisticArray: Array
    },
    components: {
        StatisticChart
    },
    methods: {

    }
}
</script>

<style></style>
