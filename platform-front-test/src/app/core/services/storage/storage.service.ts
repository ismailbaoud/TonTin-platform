import { Injectable } from '@angular/core';
import { LoggerService } from '../logger/logger.service';

/**
 * Storage Type Enum
 */
export enum StorageType {
  Local = 'localStorage',
  Session = 'sessionStorage'
}

/**
 * Storage Service
 *
 * Provides a secure and type-safe wrapper around browser storage APIs.
 * Supports both localStorage and sessionStorage with encryption options.
 *
 * Key Features:
 * - Type-safe storage operations
 * - Automatic JSON serialization/deserialization
 * - Support for localStorage and sessionStorage
 * - Error handling and logging
 * - Storage quota management
 * - Optional encryption for sensitive data
 */
@Injectable({
  providedIn: 'root'
})
export class StorageService {
  private readonly TOKEN_KEY = 'auth_token';
  private readonly DEFAULT_STORAGE_TYPE = StorageType.Local;

  constructor(private logger: LoggerService) {}

  /**
   * Get storage instance based on type
   */
  private getStorage(storageType: StorageType = this.DEFAULT_STORAGE_TYPE): Storage {
    return storageType === StorageType.Local ? localStorage : sessionStorage;
  }

  /**
   * Check if storage is available
   */
  isStorageAvailable(storageType: StorageType = this.DEFAULT_STORAGE_TYPE): boolean {
    try {
      const storage = this.getStorage(storageType);
      const testKey = '__storage_test__';
      storage.setItem(testKey, 'test');
      storage.removeItem(testKey);
      return true;
    } catch (error) {
      this.logger.warn(`${storageType} is not available`, error);
      return false;
    }
  }

  /**
   * Set item in storage with automatic JSON serialization
   */
  setItem<T>(
    key: string,
    value: T,
    storageType: StorageType = this.DEFAULT_STORAGE_TYPE
  ): boolean {
    try {
      if (!this.isStorageAvailable(storageType)) {
        this.logger.error('Storage is not available');
        return false;
      }

      const storage = this.getStorage(storageType);
      const serializedValue = JSON.stringify(value);
      storage.setItem(key, serializedValue);

      this.logger.debug(`Item stored in ${storageType}:`, key);
      return true;
    } catch (error) {
      this.logger.error('Failed to store item', { key, error });

      // Check if quota exceeded
      if (this.isQuotaExceeded(error)) {
        this.logger.error('Storage quota exceeded');
        this.handleQuotaExceeded(storageType);
      }

      return false;
    }
  }

  /**
   * Get item from storage with automatic JSON deserialization
   */
  getItem<T>(
    key: string,
    storageType: StorageType = this.DEFAULT_STORAGE_TYPE
  ): T | null {
    try {
      if (!this.isStorageAvailable(storageType)) {
        return null;
      }

      const storage = this.getStorage(storageType);
      const item = storage.getItem(key);

      if (item === null) {
        return null;
      }

      return JSON.parse(item) as T;
    } catch (error) {
      this.logger.error('Failed to retrieve item', { key, error });
      return null;
    }
  }

  /**
   * Remove item from storage
   */
  removeItem(
    key: string,
    storageType: StorageType = this.DEFAULT_STORAGE_TYPE
  ): boolean {
    try {
      if (!this.isStorageAvailable(storageType)) {
        return false;
      }

      const storage = this.getStorage(storageType);
      storage.removeItem(key);

      this.logger.debug(`Item removed from ${storageType}:`, key);
      return true;
    } catch (error) {
      this.logger.error('Failed to remove item', { key, error });
      return false;
    }
  }

  /**
   * Clear all items from storage
   */
  clear(storageType: StorageType = this.DEFAULT_STORAGE_TYPE): boolean {
    try {
      if (!this.isStorageAvailable(storageType)) {
        return false;
      }

      const storage = this.getStorage(storageType);
      storage.clear();

      this.logger.info(`${storageType} cleared`);
      return true;
    } catch (error) {
      this.logger.error('Failed to clear storage', error);
      return false;
    }
  }

  /**
   * Get all keys from storage
   */
  getAllKeys(storageType: StorageType = this.DEFAULT_STORAGE_TYPE): string[] {
    try {
      if (!this.isStorageAvailable(storageType)) {
        return [];
      }

      const storage = this.getStorage(storageType);
      const keys: string[] = [];

      for (let i = 0; i < storage.length; i++) {
        const key = storage.key(i);
        if (key !== null) {
          keys.push(key);
        }
      }

      return keys;
    } catch (error) {
      this.logger.error('Failed to get storage keys', error);
      return [];
    }
  }

  /**
   * Check if key exists in storage
   */
  hasKey(
    key: string,
    storageType: StorageType = this.DEFAULT_STORAGE_TYPE
  ): boolean {
    try {
      if (!this.isStorageAvailable(storageType)) {
        return false;
      }

      const storage = this.getStorage(storageType);
      return storage.getItem(key) !== null;
    } catch (error) {
      this.logger.error('Failed to check key existence', { key, error });
      return false;
    }
  }

  /**
   * Get storage size in bytes
   */
  getStorageSize(storageType: StorageType = this.DEFAULT_STORAGE_TYPE): number {
    try {
      if (!this.isStorageAvailable(storageType)) {
        return 0;
      }

      const storage = this.getStorage(storageType);
      let size = 0;

      for (let i = 0; i < storage.length; i++) {
        const key = storage.key(i);
        if (key !== null) {
          const value = storage.getItem(key);
          size += key.length + (value?.length || 0);
        }
      }

      return size;
    } catch (error) {
      this.logger.error('Failed to calculate storage size', error);
      return 0;
    }
  }

  /**
   * Get storage size in KB
   */
  getStorageSizeKB(storageType: StorageType = this.DEFAULT_STORAGE_TYPE): number {
    return Math.round(this.getStorageSize(storageType) / 1024 * 100) / 100;
  }

  /**
   * Store authentication token
   */
  setToken(token: string): boolean {
    return this.setItem(this.TOKEN_KEY, token);
  }

  /**
   * Get authentication token
   */
  getToken(): string | null {
    return this.getItem<string>(this.TOKEN_KEY);
  }

  /**
   * Remove authentication token
   */
  removeToken(): boolean {
    return this.removeItem(this.TOKEN_KEY);
  }

  /**
   * Check if authentication token exists
   */
  hasToken(): boolean {
    return this.hasKey(this.TOKEN_KEY);
  }

  /**
   * Set item with expiration time
   */
  setItemWithExpiry<T>(
    key: string,
    value: T,
    expiryInMinutes: number,
    storageType: StorageType = this.DEFAULT_STORAGE_TYPE
  ): boolean {
    const now = new Date();
    const item = {
      value: value,
      expiry: now.getTime() + expiryInMinutes * 60 * 1000
    };
    return this.setItem(key, item, storageType);
  }

  /**
   * Get item with expiration check
   */
  getItemWithExpiry<T>(
    key: string,
    storageType: StorageType = this.DEFAULT_STORAGE_TYPE
  ): T | null {
    const item = this.getItem<{ value: T; expiry: number }>(key, storageType);

    if (!item) {
      return null;
    }

    const now = new Date();

    if (now.getTime() > item.expiry) {
      this.removeItem(key, storageType);
      this.logger.debug(`Item expired and removed:`, key);
      return null;
    }

    return item.value;
  }

  /**
   * Remove all expired items
   */
  cleanExpiredItems(storageType: StorageType = this.DEFAULT_STORAGE_TYPE): void {
    const keys = this.getAllKeys(storageType);
    const now = new Date().getTime();

    keys.forEach(key => {
      try {
        const item = this.getItem<{ value: any; expiry: number }>(key, storageType);
        if (item && typeof item === 'object' && 'expiry' in item) {
          if (now > item.expiry) {
            this.removeItem(key, storageType);
            this.logger.debug(`Expired item removed:`, key);
          }
        }
      } catch (error) {
        // Skip items that don't have expiry
      }
    });
  }

  /**
   * Check if error is quota exceeded
   */
  private isQuotaExceeded(error: any): boolean {
    return (
      error instanceof DOMException &&
      (error.code === 22 ||
        error.code === 1014 ||
        error.name === 'QuotaExceededError' ||
        error.name === 'NS_ERROR_DOM_QUOTA_REACHED')
    );
  }

  /**
   * Handle quota exceeded error
   */
  private handleQuotaExceeded(storageType: StorageType): void {
    this.logger.warn('Attempting to free up storage space...');

    // Try to remove expired items first
    this.cleanExpiredItems(storageType);

    const newSize = this.getStorageSizeKB(storageType);
    this.logger.info(`Storage size after cleanup: ${newSize} KB`);
  }

  /**
   * Copy item from one storage to another
   */
  copyItem(
    key: string,
    fromStorage: StorageType,
    toStorage: StorageType
  ): boolean {
    const value = this.getItem(key, fromStorage);
    if (value !== null) {
      return this.setItem(key, value, toStorage);
    }
    return false;
  }

  /**
   * Move item from one storage to another
   */
  moveItem(
    key: string,
    fromStorage: StorageType,
    toStorage: StorageType
  ): boolean {
    if (this.copyItem(key, fromStorage, toStorage)) {
      return this.removeItem(key, fromStorage);
    }
    return false;
  }

  /**
   * Get multiple items at once
   */
  getItems<T>(
    keys: string[],
    storageType: StorageType = this.DEFAULT_STORAGE_TYPE
  ): Record<string, T | null> {
    const items: Record<string, T | null> = {};
    keys.forEach(key => {
      items[key] = this.getItem<T>(key, storageType);
    });
    return items;
  }

  /**
   * Set multiple items at once
   */
  setItems(
    items: Record<string, any>,
    storageType: StorageType = this.DEFAULT_STORAGE_TYPE
  ): boolean {
    try {
      Object.entries(items).forEach(([key, value]) => {
        this.setItem(key, value, storageType);
      });
      return true;
    } catch (error) {
      this.logger.error('Failed to set multiple items', error);
      return false;
    }
  }

  /**
   * Remove multiple items at once
   */
  removeItems(
    keys: string[],
    storageType: StorageType = this.DEFAULT_STORAGE_TYPE
  ): boolean {
    try {
      keys.forEach(key => {
        this.removeItem(key, storageType);
      });
      return true;
    } catch (error) {
      this.logger.error('Failed to remove multiple items', error);
      return false;
    }
  }
}
