# Binary Ninjaz - Prize Justification

## EOH: A Microsoft Partner - Innovation

The Harvest system is targeted at people who are not the most computer literate. Having this target audience has meant that our system must be elementary to use for anyone who picks it up. However, as people become accustomed to the system and the interface, we would like to provide them with more power. To do this, we have employed progressive disclosure into the user interface (UI) design of the system. This goal is crucial to us because we want to give as much power to people who may not be able to use extensive systems easily.

Areas, where we have implemented progressive disclosure into the, are information entering, statistics creation and search functionality.

To achieve giving the user a progressively more powerful search functionality we implemented our domain specific language (DSL). We give the user as much power without having them learn a complicated system.

Some examples of search functionality:

- "Peter": shows all items that have some field that contains the word peter.
- "peter or john": shows all items that have some field that contains the word peter or john.
- "peter john": shows all items that have some field that contains the word peter and some field (other or the same as Peter) that contains the word john.
- "foreman peter": shows all items with a foreman that is named Peter.
- "worker john sum": shows all items with a worker called John. Also shows the sum of the yield collected.
- "last 3 days orchard name block a": shows all sessions in the last three days that had work done in the orchard named "block A".

## Retrorabbit - Algorithmic Innovation

While developing our system we encountered challenges that required using specific algorithms. We would like to discuss three design challenges that required us to turn to specific algorithms.

### Definitions:
- Yield: The amount of produce collected on a farm.
- Session: Where workers collect produce for a period.

### Genetic Algorithm

One of the requirements for our system was to provide expected yields for farmers. This requirement proved to be challenging for a couple of reasons. We did not have any historical data of previous yields to base our models on if we were to use something such as a neural network. Secondly, There are many different considerations to take into account, factors such as farm size, crop and seasonal factors, which would mean we would need a flexible solution. To solve these problems we turned to use a genetic algorithm to help us minimise the error rate of a sinusoidal function.

### Convex Hull

For a farmer to see how orchards are performing orchards, need to be mapped out on a map in the form of a polygon. However, people found performing this task not to be straight forward. So instead we decided we should build an inferred orchard area from the points of sessions that selected a particular orchard. We were able to produce great results for users using the Graham scan convex hull algorithm.

### Ray Casting

To reduce the chances of breaking referential integrity in our system we decided to make points not tied to anything other than a session. This decision meant that we needed to calculate where pickups were made. Hence, the problem essentially broke down to if a polygon contains a point. In computer graphics, this is a commonly known problem. Hence we were turned to using a ray casting algorithm.

## Qode Health (Pty) Ltd - Triple Bottom Line

Harvest aims to provide an easily accessible and cheap solution for farms of all sizes. While there are farms that have advanced tracking and monitoring of their workers and crops these are usually large farms that can easily afford to pay the costs involved.

The Harvest system is built to use as little resources as possible. We only rely on mobile GPS systems which are now very commonly found in phones. Along with an internet connection, there's nothing else one needs to start using the Harvest system.

As price is a concern, we needed to minimise internet costs. To do this, we employed caching on the mobile systems. Our use of a real-time database also meant we could have more fine grain control over downloads and uploads. Since the database supported notifications, we only download data for a user when data is updated on the server once, but keeping everything in sync.

Continuing on the note of reducing costs we employed subsystems in Harvest to help prevent theft. Theft is a significant concern for many farmers who cannot always easily monitor their workers. The Harvest system has real-time location tracking of foremen during a working session. We provide stats to help see both underperforming workers and areas. For instance, one can look at a heat map and see where an area is not doing so well.
