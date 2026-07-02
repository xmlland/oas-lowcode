export const getZIndex = () => {
    let zIndexCount = window.$zIndexCount || 0
    zIndexCount++
    window.$zIndexCount = zIndexCount
    return 1000 + zIndexCount * 2
}
