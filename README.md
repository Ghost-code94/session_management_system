# Dual-Key Session Management System  
### Secure Orchestration for Multimodal Distributed Systems

---

## Overview

The **Dual-Key Session Management System** is a framework-agnostic session and CSRF security engine designed for modern distributed applications.

Unlike traditional session systems that rely on a single session identifier, this system uses **two cryptographically distinct but logically bound identifiers**:

- **Session Identifier (SID)**
- **CSRF Identifier (CSRF-ID)**

These identifiers are generated, stored, validated, rotated, and invalidated **atomically** as a single bound record.  
This design eliminates token desynchronization, strengthens CSRF protection, and enables **early session reconstruction** for request orchestration *before* routing occurs.

The system is optimized for:
- Cross-domain web applications
- API-first architectures
- Micro-services & service meshes
- Multimodal workflows (image, video, data, AI)
- Low-latency distributed session stores (e.g., Redis)

---

## Key Features

✅ Cryptographically secure 256-bit session identifiers  
✅ Separate CSRF token bound to the session record  
✅ Atomic creation, rotation, and invalidation  
✅ Hybrid `SameSite` cookie configuration support  
✅ Constant-time CSRF token comparison  
✅ Pre-routing session reconstruction  
✅ Framework-agnostic core (Ktor / Spring / Node adapters supported)  
✅ Designed for distributed and multimodal orchestration systems  

---

## Architecture Summary

### Core Concepts

| Component | Description |
|--------|-------------|
| Session ID (SID) | Opaque random identifier used to load session state |
| CSRF ID | Independent cryptographic token bound to the session |
| Dual-Key Record | Single serialized object storing SID + CSRF + session |
| Session Manager | Engine responsible for lifecycle and validation |
| Storage | Pluggable low-latency key-value store |

### Design Principle

> **Possession of one key alone is insufficient.**  
> Both the session identifier *and* the CSRF identifier must validate for a write operation to succeed.

---

## Package Structure

```text
dual-session-core/
├─ config/        # Cookie + TTL configuration
├─ model/         # Session, cookie, and record models
├─ storage/       # Storage abstractions
├─ crypto/        # Secure ID generation & constant-time comparison
└─ engine/        # Dual-key session management engine
