package com.kkd.productsearchservice;

import java.util.List;

import org.bson.Document;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "farmer-product-service")
public interface FarmerProductServiceProxy {
	@GetMapping("/search-key/{location}/{searchKey}")
	public ResponseEntity<List<Document>> getSearchProducts(@PathVariable(value = "searchKey") String searchKey,
			@PathVariable(value = "location") String location);
}
