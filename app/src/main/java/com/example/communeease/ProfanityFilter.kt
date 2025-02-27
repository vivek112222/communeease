package com.example.communeease

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

class TrieNode {
    val children = HashMap<Char, TrieNode>()
    var isEndOfWord = false
}

class ProfanityFilter(private val context: Context) {
    private val root = TrieNode()

    // Load words from badwords.txt
    fun loadBadWords() {
        try {
            val inputStream = context.assets.open("badwords.txt")
            val reader = BufferedReader(InputStreamReader(inputStream))
            reader.forEachLine { insert(it.trim().lowercase()) }
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Insert words into Trie
    private fun insert(word: String) {
        var current = root
        for (char in word) {
            current = current.children.getOrPut(char) { TrieNode() }
        }
        current.isEndOfWord = true
    }

    // Check if a word is in the Trie
    private fun search(word: String): Boolean {
        var current = root
        for (char in word) {
            current = current.children[char] ?: return false
        }
        return current.isEndOfWord
    }

    // Censor bad words in a sentence
    fun censorText(text: String): String {
        val words = text.split(" ")
        return words.joinToString(" ") { word ->
            if (search(word.lowercase())) "*".repeat(word.length) else word
        }
    }
}
