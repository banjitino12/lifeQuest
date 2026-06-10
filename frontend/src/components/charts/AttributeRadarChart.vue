<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue';
import * as echarts from 'echarts';

import type { AttributeViewModel } from '@/types';

const props = defineProps<{
  attributes: AttributeViewModel[];
}>();

const chartRef = ref<HTMLDivElement>();
let chart: echarts.ECharts | null = null;

function renderChart() {
  if (!chartRef.value) {
    return;
  }

  chart ??= echarts.init(chartRef.value);
  chart.setOption({
    color: ['#1e7f5c'],
    radar: {
      radius: '64%',
      indicator: props.attributes.map((item) => ({
        name: item.label,
        max: 100,
      })),
      splitArea: {
        areaStyle: {
          color: ['rgba(30, 127, 92, 0.05)', 'rgba(30, 127, 92, 0.1)'],
        },
      },
      axisName: {
        color: '#5f6f82',
        fontSize: 12,
      },
    },
    series: [
      {
        type: 'radar',
        areaStyle: {
          color: 'rgba(30, 127, 92, 0.22)',
        },
        lineStyle: {
          color: '#1e7f5c',
          width: 2,
        },
        data: [
          {
            value: props.attributes.map((item) => item.value),
          },
        ],
        symbol: 'circle',
        symbolSize: 5,
      },
    ],
  });
}

function resizeChart() {
  chart?.resize();
}

onMounted(() => {
  renderChart();
  window.addEventListener('resize', resizeChart);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeChart);
  chart?.dispose();
  chart = null;
});

watch(() => props.attributes, renderChart, { deep: true });
</script>

<template>
  <div ref="chartRef" class="attribute-radar-chart" />
</template>

