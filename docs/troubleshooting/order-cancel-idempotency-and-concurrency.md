# [트러블 슈팅] 주문 취소 요청 멱등성 보장 및 주문 상태와 재고 동시성 문제 해결 및 데드락 문제 예방

# 1. 배경

- 주문 상세 조회 페이지에서 주문 취소 버튼을 누르면 주문 엔티티의 주문 상태가 ORDERED에서 CANCELED로 변하며 주문이 취소됩니다.
- 취소한 주문과 연관 관계를 가진 주문 상품들의 주문 수량 만큼 해당 상품의 재고를 증가시킵니다.
- 주문 취소 기능은 한번에 주문한 개별 주문 상품에 대한 취소가 아닌 주문 엔티티 전체와 연관관계를 가진 주문 상품들 전체를 취소하는 것입니다.
- 현재로서 결제와 배송 등의 도메인이 없기에 주문 상태 변경과 재고 증가만 수행합니다.

# 2. 문제 상황

- **주문 취소 요청 멱등성 보장**
    - 주문 취소 요청을 여러 번 보냈을 때 재고 증가가 주문 수량 만큼이 아니라 주문 수량 * 요청 수 만큼 수행될 수 있습니다.
- **주문 상태와 재고 동시성**
    - 멀티 스레드 환경에서 주문 상태의 조회 및 변경 로직에서 주문 상태가 ORDERED인 상태를 동시에 조회하여 주문 상태가 ORDERED에서 CANCELED로 변하는 1회에만 재고를 증가 시키는 로직에서 동시성 문제가 발생할 수 있습니다. (여러 번 취소 로직 진행, 유령 재고 발생)
    - 재고 증가 로직의 데이터베이스 테이블의 특정 행의 조회 및 변경에서 여러 스레드가 동시에 재고를 조회하고 변경하는 과정에서 동시성 문제가 발생할 수 있습니다. (업데이트된 재고가 덮어씌어지는 일 때문에 증가되야할 재고가 증가 되지 않음 )
- **데드락 발생**
    - 주문에 속한 주문 상품들의 재고 증가 로직에서 데이터베이스 상품 테이블의 여러 행에 대한 락을 획득하는 순서에 따라 다른 트랜잭션과 락 획득 순서가 꼬여 데드락 상황이 발생할 수 있습니다.

# 3. 원인 분석

- **주문 취소 요청 멱득성 및 주문 상태 동시성**
    - 주문 취소 요청 시 재고 증가 로직 진행 조건 검사의 부재
    - 주문 상태 조회 시 락 획득 부재
- **재고 증가 동시성**
    - JPA에 사용으로 인해 영속성 컨텍스트에 올라와 있는 상품 수량이 현재 데이터베이스에 있는 상품 수량과 일치 하지 않을 수 있다.
    - 만약 영속성 컨텍스트에 있는 상품 수량을 바탕으로 수량을 변경하면 레이스 컨디션 발생 가능
- **데드락 발생**
    - 여러 주문 상품에 대해 대응되는 여러 상품 테이블의 행들에 대해 재고를 증가 시키기 위해 락을 획득해 나가야 하는데 이때 해당되는 상품에 대해 접근하는 다른 트랜잭션과 순서과 꼬여 데드락 발생 가능

# 4. 해결 방법

## 01. 주문 상태 변경 원자적 쿼리

**OrderRepository**

```java
@Modifying(clearAutomatically = true)
@Query("UPDATE Order o SET o.orderStatus = :toStatus " +
        "WHERE o.id = :orderId AND o.orderStatus =:fromStatus AND o.user.id = :userId")
    int updateOrderStatus(@Param("orderId") Long orderId,
                          @Param("fromStatus") OrderStatus fromStatus,
                          @Param("toStatus") OrderStatus toStatus,
                          @Param("userId") Long userId);
```

- 특정 주문 상태 (fromStatus) 일 때만 주문 상태를 변경하는 원자적 쿼리 메서드 작성
- 추가적으로 주문 취소 요청을 한 사용자가 해당 주문을 한 사용자일때만 주문 변경할 수 있도록 userId 조건도 추가

**OrderService**

```java
int updatedRows = orderRepository.updateOrderStatus(orderId, OrderStatus.ORDERED, OrderStatus.CANCELED, user.getId());

if (updatedRows == 0) {
	throw new CannotCancelOrderException("취소할 수 없는 주문입니다.");
}
```

- 주문 상태 변경 원자적 쿼리 사용
- 원자적 쿼리의 반환값이 0일 경우 이미 취소된 주문이기에 CannotCancelOrderException을 발생시켜 재고 증가 로직 등을 진행하지 않도록 한다.

 **멱등성 보장**

→ 원자적 쿼리 조건문에 특정 주문 상태일 때만 변경이 되도록 했으며, 해당 리포지토리 메서드에 OrderStatus.ORDERED를 전달하여 이미 취소된 주문인 경우 예외를 발생시켜 트랜잭션을 롤백한다.

**동시성 문제 해결**

→ 조회와 변경을 원자적 쿼리로 처리해 조회된 결과와 데이터베이스의 상태가 다르고, 다른 스레드가 동일한 주문에 대해 취소 로직을 실행하여도 동시성 문제가 발생하지 않는다.

## 02. 상품 재고 증가 원자적 쿼리

**ProductRepository**

```java
@Modifying(clearAutomatically = true)
@Query("UPDATE Product p SET p.stock = p.stock + :quantity WHERE p.id =:productd")
    void restoreStock(@Param("productId") Long productId, @Param("quantity") int quantity);
```

- 상품 테이블의 저장된 특정 행의 상품 재고에 quantity (주문 수량) 더하는 원자적 쿼리 사용

**OrderService**

```java
for (OrderItem item : orderItems) {
		productRepository.restoreStock(item.getProduct().getId(), item.getQuantity());
}
```

- 특정 주문에 속한 모든 주문 상품에 대해 주문 수량 만큼 원자적 쿼리로 재고 증가

**동시성 문제 해결**

→ 영속성 컨텍스트에 있는 상품의 재고에 주문 상품의 수량을 더하는 방식으로 재고 증가 로직을 진행하면, 동시성 이슈로 영속성 컨텍스트에 있는 상품 재고가 데이터베이스에 있는 상품 재고와 달라져 의도하지 않은 결과를 발생 시킬 수 있다. 

이때 상품 엔티티 자체를 영속성 컨텍스트로 가져오지 않고, 주문 상품 엔티티의 주문 아이디와 주문 수량 정보를 이용해 상품 테이블의 저장되어 있는 상품 재고에 주문 수량을 더하는 원자적 쿼리를 사용해서 동시성 문제를 해결했다.

## 03. 상품 엔티티 접근 순서 정의 (상품 아이디 오름차순)

**OrderService**

```java
orderItems.sort(Comparator.comparing(item -> item.getProduct().getId()));

for (OrderItem item : orderItems) {
		productRepository.restoreStock(item.getProduct().getId(), item.getQuantity());
}
```

- 주문 상품들을 상품 엔티티 아이디(PK) 오름차순으로 정렬
- 오름차순으로 상품 엔티티에 접근하여 재고 수량을 증가

**데드락 방지**

→ 여러 상품 엔티티를 하나의 트랜잭션에서 접근할 때 상품 아이디 오름차순으로 접근하겠다는 전역적인 사전 약속을 통해 여러 상품 락 획득에 대해 데드락을 방지한다.

*현재 재고 증가 로직에서만 상품 아이디 오름차순으로 접근해서 데드락이 방지되는 것이 아니며, 상품 테이블에 접근하는 모든 코드에서 상품 아이디 오름차순으로 접근해야 데드락이 방지된다.

# 5. 결과

- 이미 취소된 주문 또는 연속으로 주문 취소 요청을 보낸 경우에도 한번만 주문 취소가 되며, 재고도 한번만 증가된다.
- 주문 취소에 따른 재고 증가 시 증가되야 할 재고가 증가되지 않는 문제가 방지된다.
- 데드락 문제를 예방할 수 있다.