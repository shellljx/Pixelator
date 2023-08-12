//
// Created by shell m1 on 2023/7/18.
//

#include "file.h"

std::shared_ptr<ImageTexture> ImageCache::add(const char *path, GLuint texture, int width, int height) {

  if (cache.size() >= 10) {
    cache.erase(cache.begin());
  }
  auto imageTexture = new ImageTexture();
  imageTexture->path = path;
  imageTexture->texture = texture;
  imageTexture->width = width;
  imageTexture->height = height;
  auto shared = std::shared_ptr<ImageTexture>(imageTexture);
  cache.push_back(shared);
  return shared;
}

std::shared_ptr<ImageTexture> ImageCache::get(const std::string &path) {
  for (auto it = cache.begin(); it != cache.end();) {
    if ((*it)->path == path) {
      return *it;
    }
    it++;
  }
  return nullptr;
}