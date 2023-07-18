//
// Created by shell m1 on 2023/7/18.
//

#include "file.h"

void ImageCache::add(std::shared_ptr<Effect> effect) {
  //只添加 image effect
  if (effect->type() == TypeImage) {
    if (std::find(cache.begin(), cache.end(), effect) == cache.end()) {
      if (cache.size() >= 10) {
        cache.erase(cache.begin());
      }
      cache.push_back(std::move(effect));
    }
  }
}

std::shared_ptr<ImageEffect> ImageCache::get(const std::string &path) {
  for (auto it = cache.begin(); it != cache.end();) {
    if ((*it)->type() == TypeImage) {
      auto imageEffect = std::dynamic_pointer_cast<ImageEffect>(*it);
      if (imageEffect != nullptr && imageEffect->getSrcPath() == path) {
        cache.erase(it);
        return imageEffect;
      }
    }
    it++;
  }
  return nullptr;
}