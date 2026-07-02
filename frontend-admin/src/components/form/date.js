export const patternMapping = {
    'yyyy': {
        picker: 'year',
        format: 'YYYY'
    },
    'yyyy-Q': {
        picker: 'quarter'
    },
    'yyyy-MM': {
        picker: 'month',
        format: 'YYYY-MM'
    },
    'yyyy-MM-dd': {
        picker: 'date',
        format: 'YYYY-MM-DD'
    },
    'yyyy-MM-dd HH': {
        picker: 'date', showTime: true,
        format: 'YYYY-MM-DD HH'
    },
    'yyyy-MM-dd HH:mm': {
        picker: 'date', showTime: true,
        format: 'YYYY-MM-DD HH:mm'
    },
    'yyyy-MM-dd HH:mm:ss': {
        picker: 'date', showTime: true,
        format: 'YYYY-MM-DD HH:mm:ss'
    },
    'HH:mm': {
        picker: 'time',
        format: 'HH:mm'
    },
    'HH:mm:ss': {
        picker: 'time',
        format: 'HH:mm:ss'
    },
}
