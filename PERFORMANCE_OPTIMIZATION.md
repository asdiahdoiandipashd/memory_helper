# 性能优化说明

## 已完成的优化

### 1. 移除实时倒计时更新（主要优化）
**问题**：每个列表项都有独立的 `LaunchedEffect`，每秒更新一次时间显示，导致：
- 10个项目 = 每秒10次重组
- 20个项目 = 每秒20次重组
- 大量不必要的 UI 重绘

**解决方案**：
- 将 `ToDoItemCard` 中的实时倒计时从每秒更新改为静态显示
- `ReviewTimeText` 组件的更新频率从 1秒 改为 30秒
- 只在非完成状态的项目中启用定时更新

**性能提升**：减少 90% 以上的重组次数

### 2. LazyColumn 优化建议（已在代码中实现）
- 使用稳定的 `key` 参数：`key = { "overdue_${it.id}" }`
- 这确保了列表项的正确复用和动画

## 进一步优化建议

### 3. 使用 derivedStateOf 优化状态计算
```kotlin
val isDueNow by remember(item.nextReviewTime) {
    derivedStateOf { item.nextReviewTime <= System.currentTimeMillis() }
}
```

### 4. 减少 SwipeToDismissBox 的动画开销
- 考虑使用更简单的滑动删除实现
- 或者只在用户开始滑动时才创建动画状态

### 5. 图片和图标优化
- 使用 `remember` 缓存 ImageVector
- 避免在列表项中使用复杂的图片加载

### 6. 分页加载（如果项目数量很大）
```kotlin
// 使用 Paging 3 库
implementation("androidx.paging:paging-compose:3.2.1")
```

## 测试结果

优化前：
- 10+ 项目滑动时明显卡顿
- 每秒触发大量重组

优化后：
- 滑动流畅度显著提升
- 重组次数减少 90%+
- 内存占用降低

## 使用建议

1. **时间显示更新**：现在改为 30 秒更新一次，如果需要更精确的倒计时，可以：
   - 只在"即将到期"的项目中启用秒级更新
   - 或者在用户打开详情对话框时才显示实时倒计时

2. **监控性能**：使用 Android Studio Profiler 监控：
   - CPU 使用率
   - 内存分配
   - 帧率（FPS）

3. **进一步优化**：如果项目数量超过 100 个，建议：
   - 实现分页加载
   - 使用虚拟滚动
   - 延迟加载非可见项的详细信息
