#ifndef __HASH_MAP_HH
#define __HASH_MAP_HH

/*! interface to hash_map */
template<class Key, class Entry>
class HashMap : public std::tr1::unordered_map<Key,Entry> {
};

#endif
