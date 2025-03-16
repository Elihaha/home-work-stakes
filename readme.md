# Stakes Project

## Project Highlights

1. **High-Performance Service Design**
   - Core functionalities implement lock-free concurrency control
   - Supporting features utilize minimal-granularity lock management
   - Optimized for high throughput and low latency

2. **Modular Architecture**
   - Core business functionalities encapsulated in domain modules
   - Clear business logic separation and organization
   - Standardized process flow through MVC architecture
   - Clear layering and responsibility separation

## High-Performance Betting Interface Design

### Lock-Free Design Core

1. **Request Routing Strategy**
   - Request distribution based on modulo operation of betting project ID
   - Identical betting project requests are routed to the same thread pool
   - Eliminates inter-thread resource contention through lock-free processing

2. **Thread Pool Configuration Optimization**
   - Thread pool count: Fixed at 4, fully utilizing multi-core CPU resources
   - Per thread pool configuration:
     - Core thread count: 1
     - Maximum thread count: 1
     - Task queue capacity: 10000
     - Rejection policy: AbortPolicy

### Performance Advantages

1. **High Concurrency Processing Capability**
   - Implements parallel request processing through divide-and-conquer strategy
   - Avoids performance bottlenecks caused by global locks
   - Supports large-scale concurrent betting requests

2. **Low Latency Response**
   - Lock-free design reduces thread blocking
   - Independent thread pool processing minimizes queue waiting
   - Ensures rapid response to betting requests

3. **System Scalability**
   - Thread pool count adjustable based on CPU cores
   - Task queue size supports dynamic configuration
   - Easy to extend based on business requirements

### Efficient Betting List Query Design

1. **Efficient Data Structure Design**
   - Uses ConcurrentSkipListSet as the core data structure for efficient sorting and concurrent access

2. **Snapshot Mechanism Implementation**
   - Creates efficient snapshots through shallow copying using ConcurrentSkipListSet's clone() method
   - Avoids modifications to the original data structure, ensuring data consistency
   - Supports lock-free concurrent read operations

## Session Management

### Consistency Guarantee

The session management system ensures consistency through the following mechanisms:

**Concurrent Access Control**
   - Uses `ReentrantLock` for thread-safe session operations
   - Implements per-customer locking strategy to minimize contention
   - Lock timeout mechanism (1 second) to prevent deadlocks


### Session Lifecycle and Expiration Strategy

1. **Session Creation**
   - Atomic session creation with customer ID
   - Prevents duplicate sessions for the same customer

2. **Session Expiration Management**
   - Implements dual expiration handling strategies:

   a. **Real-time Check Strategy**
      - Validates session expiration on each access
      - Immediately removes expired sessions during validation
      - Advantages:
        * Ensures data consistency in real-time
        * Reduces memory usage by immediate cleanup
      - Considerations:
        * Slightly higher per-request overhead
        * Best for scenarios requiring strict session control

   b. **Scheduled Cleanup Strategy**
      - Runs periodic cleanup task every minute
      - Batch processes and removes expired sessions
      - Advantages:
        * Prevents accumulation of expired sessions in memory
        * Complements real-time expiration checks
      - Considerations:
        * Acts as a safeguard against memory pressure



## HTTP Framework Design

The Stakes project implements a lightweight HTTP framework with a focus on simplicity and efficiency. The framework is designed with the following key features:

### Core Components

1. **Annotation-based Routing**
   - Uses `@HttpMapping` annotation for route definition
   - Supports method-level mapping with HTTP methods (GET, POST, etc.)
   - URL pattern matching with path variables (e.g., `/{customerId}/session`)

2. **Controller Layer**
   - Clean controller implementation with clear separation of concerns
   - Automatic parameter binding from HTTP requests
   - Streamlined request handling flow

### Example Usage

```java
@HttpMapping(method = "GET", url = "/{customerId}/session")
public String session(String customerId) {
    return service.session(customerId);
}
```

## Key Parameter Configurations

### Session Capacity
- Maximum session count: 1,000,000
- Memory usage estimation: ~100MB (50 bytes * 1,000,000 / 1024 / 1024)
- Optimized for single server deployment

### Thread Pool Configuration

1. **StakeManager Executor Pool**
   - Executor count: 4
   - Configuration rationale:
     - Set below HTTP server thread count to avoid resource contention
     - Matches server CPU cores for optimal multi-core utilization
     - Ensures system stability and performance
   - Queue configuration:
     - Task queue capacity: 10000
     - Rejection policy: AbortPolicy
     - Design considerations:
       * Queue size limit protects system from excessive load
       * Rejection of excess requests maintains system stability
       * Filtered requests ensure efficient resource utilization







