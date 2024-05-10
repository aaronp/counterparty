# Scenarios
This file was generated using [GenDocs](../jrm/src/test/scala/mermaid/GenDocs.scala)

To regenerate, run:
```sh
sbt test:run
```

## Restaurant

When given:
```scala
List(ArraySeq(milk, butter, flour, egg, egg, egg), ArraySeq(fish, chips))
```

This is what will happen:    


```mermaid
%%{init: {"theme": "dark", 
"themeVariables": {"primaryTextColor": "grey", "secondaryTextColor": "black", "fontFamily": "Arial", "fontSize": 14, "primaryColor": "#3498db"}}}%%
sequenceDiagram
	box green restaurant
	participant restaurant.Restaurant 🖥️
	end
	box blue inventory
	participant inventory.InventoryService 🖥️
	end
	box purple supplier
	participant supplier.Marketplace 🖥️
	end
	restaurant.Restaurant 🖥️ ->> inventory.InventoryService 🖥️ : CheckInventory(Li... 
	inventory.InventoryService 🖥️ -->> restaurant.Restaurant 🖥️ : CheckInventory(Li... Returned 'HashMap(milk -> 1...
	restaurant.Restaurant 🖥️ ->> restaurant.Restaurant 🖥️ : SaveOrder(List(Ar... 
	restaurant.Restaurant 🖥️ ->> inventory.InventoryService 🖥️ : UpdateInventory(H... 
	inventory.InventoryService 🖥️ -->> restaurant.Restaurant 🖥️ : UpdateInventory(H... Returned '()'
	restaurant.Restaurant 🖥️ ->> restaurant.Restaurant 🖥️ : GetStrategy 
	restaurant.Restaurant 🖥️ ->> supplier.Marketplace 🖥️ : ReplaceStock(Hash... 
```

   

## Marketplace

When given:
```scala
Order(Map(eggs -> 3, brocoli -> 1),Address(unit,test,ave))
```

This is what will happen:    


```mermaid
%%{init: {"theme": "dark", 
"themeVariables": {"primaryTextColor": "grey", "secondaryTextColor": "black", "fontFamily": "Arial", "fontSize": 14, "primaryColor": "#3498db"}}}%%
sequenceDiagram
	box green market
	participant market.Marketplace 🖥️
	participant market.Config 🖥️
	participant market.DB 🗄️
	end
	box blue distributor
	participant distributor.everything's £100 👤
	participant distributor.vowels are £5 👤
	end
	market.Marketplace 🖥️ ->>+ distributor.everything's £100 👤 : Order(Map(eggs ->... 
	market.Marketplace 🖥️ ->> distributor.vowels are £5 👤 : Order(Map(eggs ->... 
	distributor.vowels are £5 👤 -->> market.Marketplace 🖥️ : Order(Map(eggs ->... Returned 'RFQResponse(vowel...
	distributor.everything's £100 👤 -->>- market.Marketplace 🖥️ : Order(Map(eggs ->... Returned 'RFQResponse(every...
	market.Marketplace 🖥️ ->> market.Config 🖥️ : GetConfig 
	market.Config 🖥️ -->> market.Marketplace 🖥️ : GetConfig Returned 'Settings(1 second...
	market.Marketplace 🖥️ ->> market.DB 🗄️ : SaveOrder(Order(M... 
	market.DB 🗄️ -->> market.Marketplace 🖥️ : SaveOrder(Order(M... Returned '-1271033017'
	market.Marketplace 🖥️ ->> distributor.vowels are £5 👤 : SendOrders(List(D... 
	distributor.vowels are £5 👤 -->> market.Marketplace 🖥️ : SendOrders(List(D... Returned 'Order(Map(eggs ->...
	market.Marketplace 🖥️ ->> distributor.everything's £100 👤 : SendOrders(List(D... 
	distributor.everything's £100 👤 -->> market.Marketplace 🖥️ : SendOrders(List(D... Returned 'Order(Map(brocoli...
	market.Marketplace 🖥️ ->> market.DB 🗄️ : SaveDistributors(... 
```

   

## End to End

When given:
```scala
List(ArraySeq(milk, butter, flour, egg, egg, egg), ArraySeq(fish, chips))
```

This is what will happen:    


```mermaid
%%{init: {"theme": "dark", 
"themeVariables": {"primaryTextColor": "grey", "secondaryTextColor": "black", "fontFamily": "Arial", "fontSize": 14, "primaryColor": "#3498db"}}}%%
sequenceDiagram
	box green restaurant
	participant restaurant.Restaurant 🖥️
	end
	box blue inventory
	participant inventory.InventoryService 🖥️
	end
	box purple market
	participant market.Marketplace 🖥️
	participant market.Config 🖥️
	participant market.DB 🗄️
	end
	box gray distributor
	participant distributor.vowels are £5 👤
	participant distributor.everything's £100 👤
	end
	restaurant.Restaurant 🖥️ ->> inventory.InventoryService 🖥️ : CheckInventory(Li... 
	inventory.InventoryService 🖥️ -->> restaurant.Restaurant 🖥️ : CheckInventory(Li... Returned 'HashMap(milk -> 1...
	restaurant.Restaurant 🖥️ ->> restaurant.Restaurant 🖥️ : SaveOrder(List(Ar... 
	restaurant.Restaurant 🖥️ ->> inventory.InventoryService 🖥️ : UpdateInventory(H... 
	inventory.InventoryService 🖥️ -->> restaurant.Restaurant 🖥️ : UpdateInventory(H... Returned '()'
	restaurant.Restaurant 🖥️ ->> restaurant.Restaurant 🖥️ : GetStrategy 
	restaurant.Restaurant 🖥️ ->>+ market.Marketplace 🖥️ : ReplaceStock(Hash... 
	market.Marketplace 🖥️ ->>+ distributor.vowels are £5 👤 : Order(HashMap(mil... 
	market.Marketplace 🖥️ ->> distributor.everything's £100 👤 : Order(HashMap(mil... 
	distributor.everything's £100 👤 -->> market.Marketplace 🖥️ : Order(HashMap(mil... Returned 'RFQResponse(every...
	distributor.vowels are £5 👤 -->>- market.Marketplace 🖥️ : Order(HashMap(mil... Returned 'RFQResponse(vowel...
	market.Marketplace 🖥️ ->> market.Config 🖥️ : GetConfig 
	market.Config 🖥️ -->> market.Marketplace 🖥️ : GetConfig Returned 'Settings(1 second...
	market.Marketplace 🖥️ ->> market.DB 🗄️ : SaveOrder(Order(H... 
	market.DB 🗄️ -->> market.Marketplace 🖥️ : SaveOrder(Order(H... Returned '-426000537'
	market.Marketplace 🖥️ ->> distributor.everything's £100 👤 : SendOrders(List(D... 
	distributor.everything's £100 👤 -->> market.Marketplace 🖥️ : SendOrders(List(D... Returned 'Order(HashMap(mil...
	market.Marketplace 🖥️ ->> distributor.vowels are £5 👤 : SendOrders(List(D... 
	distributor.vowels are £5 👤 -->> market.Marketplace 🖥️ : SendOrders(List(D... Returned 'Order(Map(egg -> ...
	market.Marketplace 🖥️ ->> market.DB 🗄️ : SaveDistributors(... 
	market.DB 🗄️ -->> market.Marketplace 🖥️ : SaveDistributors(... Returned '()'
	market.Marketplace 🖥️ -->>- restaurant.Restaurant 🖥️ : ReplaceStock(Hash... Returned '-426000537'
```

   