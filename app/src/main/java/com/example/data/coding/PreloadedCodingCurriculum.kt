package com.example.data.coding

import com.example.data.local.CodingChallengeEntity
import org.json.JSONArray
import org.json.JSONObject

object PreloadedCodingCurriculum {

    fun getInitialChallenges(): List<CodingChallengeEntity> {
        return listOf(
            CodingChallengeEntity(
                id = "challenge_dijkstra_graph",
                title = "Shortest Path with Min-Heap Priority Queue",
                track = "Advanced Algorithms",
                difficulty = "S-Rank Master",
                language = "Kotlin",
                description = """
                    Implement Dijkstra's Single-Source Shortest Path algorithm on a directed weighted graph with V vertices and E non-negative edges using a binary min-heap (PriorityQueue).
                    
                    Constraints:
                    - 1 <= V <= 50,000
                    - 1 <= E <= 200,000
                    - Edge weights: 0 <= weight <= 10^7
                    - Must run in O((V + E) log V) time complexity and O(V) space.
                    - If a vertex is unreachable from the source, its distance must be recorded as Int.MAX_VALUE.
                """.trimIndent(),
                timeComplexityTarget = "O((V + E) log V)",
                spaceComplexityTarget = "O(V)",
                initialCode = """
                    import java.util.PriorityQueue

                    data class Edge(val to: Int, val weight: Int)
                    data class NodeDistance(val node: Int, val dist: Int) : Comparable<NodeDistance> {
                        override fun compareTo(other: NodeDistance): Int = this.dist.compareTo(other.dist)
                    }

                    fun dijkstraShortestPath(v: Int, adj: List<List<Edge>>, source: Int): IntArray {
                        val dist = IntArray(v) { Int.MAX_VALUE }
                        val pq = PriorityQueue<NodeDistance>()
                        
                        // TODO: Initialize source distance and push into Min-Heap
                        // TODO: Implement relaxation loop with distance pruning
                        
                        return dist
                    }
                """.trimIndent(),
                userCode = """
                    import java.util.PriorityQueue

                    data class Edge(val to: Int, val weight: Int)
                    data class NodeDistance(val node: Int, val dist: Int) : Comparable<NodeDistance> {
                        override fun compareTo(other: NodeDistance): Int = this.dist.compareTo(other.dist)
                    }

                    fun dijkstraShortestPath(v: Int, adj: List<List<Edge>>, source: Int): IntArray {
                        val dist = IntArray(v) { Int.MAX_VALUE }
                        val pq = PriorityQueue<NodeDistance>()
                        
                        dist[source] = 0
                        pq.add(NodeDistance(source, 0))
                        
                        while (pq.isNotEmpty()) {
                            val (u, d) = pq.poll()
                            if (d > dist[u]) continue
                            
                            for (edge in adj[u]) {
                                val newDist = dist[u] + edge.weight
                                if (newDist < dist[edge.to]) {
                                    dist[edge.to] = newDist
                                    pq.add(NodeDistance(edge.to, newDist))
                                }
                            }
                        }
                        return dist
                    }
                """.trimIndent(),
                solutionTemplate = """
                    // Optimal S-Rank Solution: O((V + E) log V)
                    // Uses PriorityQueue with eager edge relaxation and lazy deletion pruning
                    import java.util.PriorityQueue

                    fun dijkstraShortestPath(v: Int, adj: List<List<Edge>>, source: Int): IntArray {
                        val dist = IntArray(v) { Int.MAX_VALUE }
                        val pq = PriorityQueue<NodeDistance>()
                        dist[source] = 0
                        pq.add(NodeDistance(source, 0))

                        while (pq.isNotEmpty()) {
                            val (currNode, currDist) = pq.poll()
                            if (currDist > dist[currNode]) continue // Prune stale heap entries

                            for (edge in adj[currNode]) {
                                if (dist[currNode] + edge.weight < dist[edge.to]) {
                                    dist[edge.to] = dist[currNode] + edge.weight
                                    pq.add(NodeDistance(edge.to, dist[edge.to]))
                                }
                            }
                        }
                        return dist
                    }
                """.trimIndent(),
                testCasesJson = JSONArray().apply {
                    put(JSONObject().apply {
                        put("name", "Standard Directed Graph (5 Vertices)")
                        put("input", "V=5, Edges=[(0,1,4),(0,2,2),(2,1,1),(1,3,5),(2,3,8),(3,4,3)], Source=0")
                        put("expected", "[0, 3, 2, 8, 11]")
                    })
                    put(JSONObject().apply {
                        put("name", "Disconnected Component")
                        put("input", "V=4, Edges=[(0,1,10)], Source=0")
                        put("expected", "[0, 10, 2147483647, 2147483647]")
                    })
                    put(JSONObject().apply {
                        put("name", "Dense Cycle with Cross Edges")
                        put("input", "V=6, Dense Adjacency, Source=0")
                        put("expected", "Optimal minimal path to all accessible nodes")
                    })
                }.toString(),
                isCompleted = false,
                xpReward = 200,
                tags = "Graph, Heap, Dijkstra, O((V+E)logV)"
            ),

            CodingChallengeEntity(
                id = "challenge_lru_cache",
                title = "LRU Cache with O(1) Get & Evict",
                track = "Systems & Cryptography",
                difficulty = "Hard",
                language = "Kotlin",
                description = """
                    Design and implement a data structure for Least Recently Used (LRU) cache. It should support:
                    - get(key: Int): Return value if key exists, otherwise -1. Mark as most recently used.
                    - put(key: Int, value: Int): Update or insert value. If cache reaches capacity, evict the least recently used key.
                    
                    Both get and put operations MUST run in O(1) average time complexity.
                """.trimIndent(),
                timeComplexityTarget = "O(1) amortized",
                spaceComplexityTarget = "O(Capacity)",
                initialCode = """
                    class LRUCache(private val capacity: Int) {
                        class Node(val key: Int, var value: Int) {
                            var prev: Node? = null
                            var next: Node? = null
                        }

                        // TODO: Implement Doubly-Linked List pointers and HashMap
                        fun get(key: Int): Int {
                            return -1
                        }

                        fun put(key: Int, value: Int) {
                            // TODO: Add or update node, evict tail if size > capacity
                        }
                    }
                """.trimIndent(),
                userCode = """
                    class LRUCache(private val capacity: Int) {
                        class Node(val key: Int = 0, var value: Int = 0) {
                            var prev: Node? = null
                            var next: Node? = null
                        }

                        private val map = HashMap<Int, Node>()
                        private val head = Node()
                        private val tail = Node()

                        init {
                            head.next = tail
                            tail.prev = head
                        }

                        private fun removeNode(node: Node) {
                            node.prev?.next = node.next
                            node.next?.prev = node.prev
                        }

                        private fun addToHead(node: Node) {
                            node.next = head.next
                            node.prev = head
                            head.next?.prev = node
                            head.next = node
                        }

                        fun get(key: Int): Int {
                            val node = map[key] ?: return -1
                            removeNode(node)
                            addToHead(node)
                            return node.value
                        }

                        fun put(key: Int, value: Int) {
                            val existing = map[key]
                            if (existing != null) {
                                existing.value = value
                                removeNode(existing)
                                addToHead(existing)
                            } else {
                                if (map.size >= capacity) {
                                    val lru = tail.prev!!
                                    removeNode(lru)
                                    map.remove(lru.key)
                                }
                                val newNode = Node(key, value)
                                map[key] = newNode
                                addToHead(newNode)
                            }
                        }
                    }
                """.trimIndent(),
                solutionTemplate = """
                    // O(1) Doubly-Linked List + Hash Map
                    // Sentinel head and tail nodes avoid edge-case null pointer checks
                    class LRUCache(private val capacity: Int) {
                        private val cache = HashMap<Int, Node>()
                        private val head = Node(0, 0)
                        private val tail = Node(0, 0)
                        init { head.next = tail; tail.prev = head }
                        // Fast eviction from tail.prev, fast insertion at head.next
                    }
                """.trimIndent(),
                testCasesJson = JSONArray().apply {
                    put(JSONObject().apply {
                        put("name", "Capacity 2 Basic Eviction")
                        put("input", "put(1, 1), put(2, 2), get(1), put(3, 3), get(2)")
                        put("expected", "get(1)->1, get(2)->-1 (evicted), get(3)->3")
                    })
                    put(JSONObject().apply {
                        put("name", "Update Existing Key Priority")
                        put("input", "put(1, 10), put(1, 20), get(1)")
                        put("expected", "20")
                    })
                }.toString(),
                isCompleted = false,
                xpReward = 180,
                tags = "DataStructures, HashMap, DoublyLinkedList, O(1)"
            ),

            CodingChallengeEntity(
                id = "challenge_trie_wildcard",
                title = "Trie Dictionary with Wildcard Regex Match",
                track = "Advanced Algorithms",
                difficulty = "S-Rank Master",
                language = "Kotlin",
                description = """
                    Build a Trie (Prefix Tree) supporting insertion and search with a single-character wildcard '.' that can match any character.
                    
                    Methods:
                    - insert(word: String): void
                    - search(pattern: String): Boolean (can contain '.' wildcards)
                    
                    Constraints:
                    - 1 <= word.length <= 500
                    - Words consist of lowercase English letters.
                    - Search must use efficient DFS backtracking on '.' characters without redundant allocations.
                """.trimIndent(),
                timeComplexityTarget = "O(M) exact / O(26^K) worst wildcard",
                spaceComplexityTarget = "O(N * M)",
                initialCode = """
                    class WordDictionary {
                        class TrieNode {
                            val children = Array<TrieNode?>(26) { null }
                            var isEndOfWord = false
                        }

                        private val root = TrieNode()

                        fun insert(word: String) {
                            // TODO: Insert characters into Trie
                        }

                        fun search(pattern: String): Boolean {
                            // TODO: DFS match supporting '.' wildcard
                            return false
                        }
                    }
                """.trimIndent(),
                userCode = """
                    class WordDictionary {
                        class TrieNode {
                            val children = Array<TrieNode?>(26) { null }
                            var isEndOfWord = false
                        }

                        private val root = TrieNode()

                        fun insert(word: String) {
                            var curr = root
                            for (c in word) {
                                val idx = c - 'a'
                                if (curr.children[idx] == null) {
                                    curr.children[idx] = TrieNode()
                                }
                                curr = curr.children[idx]!!
                            }
                            curr.isEndOfWord = true
                        }

                        fun search(pattern: String): Boolean {
                            return matchDfs(pattern, 0, root)
                        }

                        private fun matchDfs(pattern: String, index: Int, node: TrieNode): Boolean {
                            if (index == pattern.length) return node.isEndOfWord
                            val c = pattern[index]
                            if (c == '.') {
                                for (child in node.children) {
                                    if (child != null && matchDfs(pattern, index + 1, child)) {
                                        return true
                                    }
                                }
                                return false
                            } else {
                                val idx = c - 'a'
                                val child = node.children[idx] ?: return false
                                return matchDfs(pattern, index + 1, child)
                            }
                        }
                    }
                """.trimIndent(),
                solutionTemplate = """
                    // Trie with Depth-First Backtracking on Wildcard '.'
                    // Space: O(Sum of chars across dictionary)
                    // Time: O(Pattern Length) for exact; O(26^Wildcards) worst case
                """.trimIndent(),
                testCasesJson = JSONArray().apply {
                    put(JSONObject().apply {
                        put("name", "Exact Word Match")
                        put("input", "Words=['shinobi', 'sharingan'], Query='shinobi'")
                        put("expected", "true")
                    })
                    put(JSONObject().apply {
                        put("name", "Wildcard Dot Match")
                        put("input", "Words=['itachi', 'sasuke'], Query='i.ac.i'")
                        put("expected", "true")
                    })
                    put(JSONObject().apply {
                        put("name", "Non-Matching Pattern")
                        put("input", "Words=['ninja'], Query='..x..'")
                        put("expected", "false")
                    })
                }.toString(),
                isCompleted = false,
                xpReward = 220,
                tags = "Trie, DFS, Backtracking, Strings"
            ),

            CodingChallengeEntity(
                id = "challenge_pbkdf2_aes",
                title = "PBKDF2 HMAC-SHA256 & AES-GCM Cryptographic Vault",
                track = "Systems & Cryptography",
                difficulty = "Forbidden / ANBU",
                language = "Kotlin",
                description = """
                    Implement end-to-end cryptographic key derivation and authenticated encryption:
                    1. Derive a 256-bit AES key from a human passphrase using PBKDF2WithHmacSHA256 with a 16-byte cryptographically secure salt and 100,000 iterations.
                    2. Encrypt plaintext using AES/GCM/NoPadding with a 12-byte secure IV and 128-bit authentication tag.
                    3. Format cipher payload bundling [Salt (16B) | IV (12B) | Ciphertext + Tag].
                    4. Implement decrypt verification that detects ciphertext tampering immediately via AEAD tag validation.
                """.trimIndent(),
                timeComplexityTarget = "O(Iterations * BlockSize)",
                spaceComplexityTarget = "O(KeySize)",
                initialCode = """
                    import javax.crypto.Cipher
                    import javax.crypto.SecretKeyFactory
                    import javax.crypto.spec.GCMParameterSpec
                    import javax.crypto.spec.PBEKeySpec
                    import javax.crypto.spec.SecretKeySpec
                    import java.security.SecureRandom

                    class ShinobiCryptoEngine {
                        fun deriveAndEncrypt(passphrase: String, plainText: ByteArray): ByteArray {
                            // TODO: PBKDF2 100,000 rounds + AES-GCM 128-bit tag
                            return byteArrayOf()
                        }

                        fun decrypt(passphrase: String, encryptedPayload: ByteArray): ByteArray {
                            // TODO: Extract salt, IV, ciphertext and decrypt with tag authentication
                            return byteArrayOf()
                        }
                    }
                """.trimIndent(),
                userCode = """
                    import javax.crypto.Cipher
                    import javax.crypto.SecretKeyFactory
                    import javax.crypto.spec.GCMParameterSpec
                    import javax.crypto.spec.PBEKeySpec
                    import javax.crypto.spec.SecretKeySpec
                    import java.security.SecureRandom

                    class ShinobiCryptoEngine {
                        private val random = SecureRandom()

                        fun deriveAndEncrypt(passphrase: String, plainText: ByteArray): ByteArray {
                            val salt = ByteArray(16).apply { random.nextBytes(this) }
                            val iv = ByteArray(12).apply { random.nextBytes(this) }

                            val spec = PBEKeySpec(passphrase.toCharArray(), salt, 100000, 256)
                            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                            val secretKey = SecretKeySpec(factory.generateSecret(spec).encoded, "AES")

                            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                            cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
                            val cipherText = cipher.doFinal(plainText)

                            // Bundle: salt(16) + iv(12) + cipherText
                            return salt + iv + cipherText
                        }

                        fun decrypt(passphrase: String, payload: ByteArray): ByteArray {
                            val salt = payload.copyOfRange(0, 16)
                            val iv = payload.copyOfRange(16, 28)
                            val cipherText = payload.copyOfRange(28, payload.size)

                            val spec = PBEKeySpec(passphrase.toCharArray(), salt, 100000, 256)
                            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                            val secretKey = SecretKeySpec(factory.generateSecret(spec).encoded, "AES")

                            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
                            return cipher.doFinal(cipherText)
                        }
                    }
                """.trimIndent(),
                solutionTemplate = """
                    // Standard NIST SP 800-132 & NIST SP 800-38D compliant AEAD
                    // 16-byte random salt, 100,000 PBKDF2 rounds, 12-byte GCM IV, 128-bit authentication tag
                """.trimIndent(),
                testCasesJson = JSONArray().apply {
                    put(JSONObject().apply {
                        put("name", "Key Derivation & Roundtrip Verification")
                        put("input", "Passphrase='TsukuyomiMaster2026', PlainText='Classified Scroll Data'")
                        put("expected", "Decrypted plaintext matches original byte-for-byte")
                    })
                    put(JSONObject().apply {
                        put("name", "Tamper Detection on Ciphertext")
                        put("input", "1 bit flip in ciphertext payload")
                        put("expected", "AEADBadTagException / Decryption failure thrown")
                    })
                }.toString(),
                isCompleted = false,
                xpReward = 250,
                tags = "Security, AES-256-GCM, PBKDF2, Cryptography"
            ),

            CodingChallengeEntity(
                id = "challenge_lockfree_queue",
                title = "Lock-Free Ring Buffer (MPSC)",
                track = "Concurrency & Parallelism",
                difficulty = "Forbidden / ANBU",
                language = "Kotlin",
                description = """
                    Implement a Lock-Free Multi-Producer Single-Consumer (MPSC) bounded circular Ring Buffer in Kotlin.
                    
                    Requirements:
                    - Non-blocking enqueue using Compare-And-Swap (AtomicLong / AtomicReferenceArray).
                    - Never use synchronized, ReentrantLock, or blocking sleep primitives.
                    - Guarantee zero lost updates when multiple threads enqueue simultaneously.
                    - Support fast poll() from the single consumer thread.
                """.trimIndent(),
                timeComplexityTarget = "O(1) wait-free enqueue / dequeue",
                spaceComplexityTarget = "O(Capacity)",
                initialCode = """
                    import java.util.concurrent.atomic.AtomicLong
                    import java.util.concurrent.atomic.AtomicReferenceArray

                    class LockFreeRingBuffer<T>(val capacity: Int) {
                        private val buffer = AtomicReferenceArray<T?>(capacity)
                        private val head = AtomicLong(0)
                        private val tail = AtomicLong(0)

                        fun offer(item: T): Boolean {
                            // TODO: Implement CAS loop with sequence cursor
                            return false
                        }

                        fun poll(): T? {
                            // TODO: Single-consumer dequeue
                            return null
                        }
                    }
                """.trimIndent(),
                userCode = """
                    import java.util.concurrent.atomic.AtomicLong
                    import java.util.concurrent.atomic.AtomicReferenceArray

                    class LockFreeRingBuffer<T>(val capacity: Int) {
                        private val buffer = AtomicReferenceArray<T?>(capacity)
                        private val head = AtomicLong(0) // Consumer sequence
                        private val tail = AtomicLong(0) // Producer sequence

                        fun offer(item: T): Boolean {
                            while (true) {
                                val currentTail = tail.get()
                                val currentHead = head.get()
                                if (currentTail - currentHead >= capacity) {
                                    return false // Buffer full
                                }
                                val index = (currentTail % capacity).toInt()
                                if (buffer.get(index) == null) {
                                    if (tail.compareAndSet(currentTail, currentTail + 1)) {
                                        buffer.set(index, item)
                                        return true
                                    }
                                }
                            }
                        }

                        fun poll(): T? {
                            val currentHead = head.get()
                            val currentTail = tail.get()
                            if (currentHead >= currentTail) return null // Empty
                            val index = (currentHead % capacity).toInt()
                            val item = buffer.get(index) ?: return null
                            buffer.set(index, null)
                            head.lazySet(currentHead + 1)
                            return item
                        }
                    }
                """.trimIndent(),
                solutionTemplate = """
                    // Lock-Free MPSC Ring Buffer with CAS Atomic Sequence
                    // Avoids false sharing and cache-line contention
                """.trimIndent(),
                testCasesJson = JSONArray().apply {
                    put(JSONObject().apply {
                        put("name", "High Concurrency Multi-Thread Offer")
                        put("input", "8 Producer Threads, 10,000 items each")
                        put("expected", "80,000 items polled in FIFO sequence without deadlock")
                    })
                }.toString(),
                isCompleted = false,
                xpReward = 260,
                tags = "Concurrency, LockFree, CAS, Atomic, MPSC"
            ),

            CodingChallengeEntity(
                id = "challenge_bitmask_tsp",
                title = "Bitmask DP: Hamiltonian Cycle with Memoization",
                track = "Dynamic Programming",
                difficulty = "S-Rank Master",
                language = "Kotlin",
                description = """
                    Given N shinobi outposts and an N x N cost matrix representing direct travel energy between any two outposts, calculate the minimum energy required to visit all outposts exactly once and return to the starting outpost 0.
                    
                    Constraints:
                    - 2 <= N <= 18
                    - Cost matrix symmetric with 0 <= cost[i][j] <= 10,000
                    - Must employ bitmask state compression (mask: Int, city: Int) with memoization.
                    - Naive brute-force O(N!) will timeout; required complexity is O(N^2 * 2^N).
                """.trimIndent(),
                timeComplexityTarget = "O(N^2 * 2^N)",
                spaceComplexityTarget = "O(N * 2^N)",
                initialCode = """
                    fun minHamiltonianCycleCost(n: Int, cost: Array<IntArray>): Int {
                        val memo = Array(1 shl n) { IntArray(n) { -1 } }
                        
                        // TODO: Implement recursive bitmask DP with memoization
                        return 0
                    }
                """.trimIndent(),
                userCode = """
                    fun minHamiltonianCycleCost(n: Int, cost: Array<IntArray>): Int {
                        val memo = Array(1 shl n) { IntArray(n) { -1 } }

                        fun solve(mask: Int, u: Int): Int {
                            if (mask == (1 shl n) - 1) {
                                return cost[u][0] // Return to origin
                            }
                            if (memo[mask][u] != -1) return memo[mask][u]

                            var minCost = Int.MAX_VALUE / 2
                            for (v in 0 until n) {
                                if ((mask and (1 shl v)) == 0) {
                                    val nextCost = cost[u][v] + solve(mask or (1 shl v), v)
                                    if (nextCost < minCost) {
                                        minCost = nextCost
                                    }
                                }
                            }
                            memo[mask][u] = minCost
                            return minCost
                        }

                        return solve(1, 0)
                    }
                """.trimIndent(),
                solutionTemplate = """
                    // Bitmask Dynamic Programming: O(N^2 * 2^N)
                    // State: mask represents visited set of vertices, u represents current city
                """.trimIndent(),
                testCasesJson = JSONArray().apply {
                    put(JSONObject().apply {
                        put("name", "4 Outposts Standard Matrix")
                        put("input", "N=4, Cost=[[0,20,42,25],[20,0,30,34],[42,30,0,10],[25,34,10,0]]")
                        put("expected", "85")
                    })
                    put(JSONObject().apply {
                        put("name", "Symmetric 5 Outpost Cluster")
                        put("input", "N=5, Dense Network")
                        put("expected", "Optimal Hamiltonian cycle computed in < 2ms")
                    })
                }.toString(),
                isCompleted = false,
                xpReward = 240,
                tags = "DynamicProgramming, Bitmask, TSP, O(N^2 2^N)"
            )
        )
    }
}
