//
//  GXSliderNode.h
//  GaiaXiOS
//
//  Copyright (c) 2021, Alibaba Group Holding Limited.
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.

#import "GXBaseNode.h"

NS_ASSUME_NONNULL_BEGIN

@interface GXSliderNode : GXBaseNode

// 列数
@property (nonatomic, assign) NSInteger column;
// 是否可以滚动
@property (nonatomic, assign) BOOL scrollEnable;
// 内边距
@property (nonatomic) UIEdgeInsets contentInset;
// 与坑位方向垂直的间距
@property (nonatomic, assign) CGFloat rowSpacing;
// 坑位间距
@property (nonatomic, assign) CGFloat itemSpacing;

@end

NS_ASSUME_NONNULL_END
