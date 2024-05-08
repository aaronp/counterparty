# Scenarios
This file was generated using [GenDocs](../jrm/src/test/scala/mermaid/GenDocs.scala)

To regenerate, run:
```sh
sbt test:run
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
	participant distributor.everything's £100 👤
	participant distributor.vowels are £5 👤
	end
	restaurant.Restaurant 🖥️ ->> inventory.InventoryService 🖥️ : CheckInventory(List(milk, butter, flour, egg, egg, egg, fish, chips))
	inventory.InventoryService 🖥️ -->> restaurant.Restaurant 🖥️ : Returned 'HashMap(milk -> 1, egg -> 3, flour -> 1, fish -> 1, butter -> 1, chips -> 1)'
	restaurant.Restaurant 🖥️ ->> restaurant.Restaurant 🖥️ : SaveOrder(List(ArraySeq(milk, butter, flour, egg, egg, egg), ArraySeq(fish, chips))) Returned 'order-2'
	restaurant.Restaurant 🖥️ ->> inventory.InventoryService 🖥️ : UpdateInventory(HashMap(milk -> 0, egg -> 0, flour -> 0, fish -> 0, butter -> 0, c...
	inventory.InventoryService 🖥️ -->> restaurant.Restaurant 🖥️ : Returned '()'
	restaurant.Restaurant 🖥️ ->> restaurant.Restaurant 🖥️ : GetStrategy Returned 'Strategy(30,7)'
	restaurant.Restaurant 🖥️ ->>+ market.Marketplace 🖥️ : ReplaceStock(HashMap(milk -> 28, egg -> 21, flour -> 28, fish -> 28, butter -> 28,...
	market.Marketplace 🖥️ ->>+ distributor.everything's £100 👤 : Order(HashMap(milk -> 28, egg -> 21, flour -> 28, fish -> 28, butter -> 28, chips ...
	market.Marketplace 🖥️ ->> distributor.vowels are £5 👤 : Order(HashMap(milk -> 28, egg -> 21, flour -> 28, fish -> 28, butter -> 28, chips ...
	distributor.vowels are £5 👤 -->> market.Marketplace 🖥️ : Returned 'RFQResponse(vowels are £5,HashMap(egg -> 5.0))'
	distributor.everything's £100 👤 -->>- market.Marketplace 🖥️ : Returned 'RFQResponse(everything's £100,HashMap(milk -> 100.0, egg -> 100.0, flour -> 100.0, fish -> 100.0, butter -> 100.0, chips -> 100.0))'
	market.Marketplace 🖥️ ->> market.Config 🖥️ : GetConfig
	market.Config 🖥️ -->> market.Marketplace 🖥️ : Returned 'Settings(1 second,Address(Override,Street,Eyam))'
	market.Marketplace 🖥️ ->> market.DB 🗄️ : SaveOrder(Order(HashMap(milk -> 28, egg -> 21, flour -> 28, fish -> 28, butter -> ...
	market.DB 🗄️ -->> market.Marketplace 🖥️ : Returned '-426000537'
	market.Marketplace 🖥️ ->>+ distributor.vowels are £5 👤 : SendOrders(List(DistributorOrder(everything's £100,Order(HashMap(milk -> 28, flour...
	market.Marketplace 🖥️ ->> distributor.everything's £100 👤 : SendOrders(List(DistributorOrder(everything's £100,Order(HashMap(milk -> 28, flour...
	distributor.everything's £100 👤 -->> market.Marketplace 🖥️ : Returned 'Order(HashMap(milk -> 28, flour -> 28, fish -> 28, butter -> 28, chips -> 28),Address(Override,Street,Eyam))'
	distributor.vowels are £5 👤 -->>- market.Marketplace 🖥️ : Returned 'Order(Map(egg -> 21),Address(Override,Street,Eyam))'
	market.Marketplace 🖥️ ->> market.DB 🗄️ : SaveDistributors(-426000537,Map(everything's £100 -> -426000537-2075200391, vowels...
	market.DB 🗄️ -->> market.Marketplace 🖥️ : Returned '()'
	market.Marketplace 🖥️ -->>- restaurant.Restaurant 🖥️ : Returned '-426000537'
```

   